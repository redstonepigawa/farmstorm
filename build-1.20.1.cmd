@echo off
rem ============================================================
rem  Farmstorm 1.20.1  Phase 1 / M1  -  workspace build wrapper
rem  Project : %FARMSTORM_PROBE% (default D:\project_farm1.20.1\probe)
rem  Toolchain: reused READ-ONLY from D:\project_farm\.tools
rem
rem  Usage:
rem    build-1.20.1.cmd                  gradle build
rem    build-1.20.1.cmd :dependencies    any gradle task / args (passed through)
rem    build-1.20.1.cmd --nd build       same, but add --no-daemon
rem    build-1.20.1.cmd --dry build      parse and print the command, run nothing
rem    BUILD_DEBUG=1 then build-1.20.1.cmd
rem
rem  Exit codes: 0 ok / gradle exit code otherwise /
rem              2 missing toolchain file / 3 no build.gradle / 4 java cannot write project
rem ============================================================
setlocal EnableExtensions
if /i "%BUILD_DEBUG%"=="1" echo on

rem ---- parse wrapper-only flags; everything else goes to gradle ----
set "TASKS="
set "NODAEMON="
set "DRY="
:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--nd"        (set "NODAEMON=--no-daemon" & shift & goto parse)
if /i "%~1"=="--no-daemon" (set "NODAEMON=--no-daemon" & shift & goto parse)
if /i "%~1"=="--dry"       (set "DRY=1" & shift & goto parse)
set "TASKS=%TASKS% %1"
shift
goto parse
:parsed
if defined TASKS goto haveTasks
set "TASKS= build"
:haveTasks

rem ---- pinned toolchain (docs/zh/02-TECH-1.20.1.md section 2) ----
rem NOTE: do NOT export JDK_JAVA_OPTIONS. The java.base patch lives in
rem org.gradle.jvmargs (see probe/gradle.properties). Exported as an env var
rem it leaks into Gradle's JDK-17 toolchain probe and makes Java 17 invalid.
set "JAVA_HOME=D:\project_farm\.tools\jdk21"
set "GRADLE_USER_HOME=D:\project_farm\.tools\gradle-home"
set "TEMP=D:\project_farm\.tools\tmp"
set "TMP=D:\project_farm\.tools\tmp"
set "JAVA_TOOL_OPTIONS=-Duser.language=en -Duser.country=US -XX:ActiveProcessorCount=2"
set "GRADLE_BIN=D:\project_farm\.tools\gradle-8.10\bin\gradle.bat"
set "INIT=D:\project_farm\.tools\init.gradle"
if not defined FARMSTORM_PROBE set "FARMSTORM_PROBE=D:\project_farm1.20.1"
set "PROBE=%FARMSTORM_PROBE%"
set "LOG=D:\project_farm1.20.1\build-1.20.1.log"

rem ---- preconditions, so a failure names the missing piece ----
if not exist "%GRADLE_BIN%" (echo [build-1.20.1] FATAL: gradle not found & exit /b 2)
if not exist "%INIT%" (echo [build-1.20.1] FATAL: init script not found & exit /b 2)
if not exist "%PROBE%\build.gradle" (echo [build-1.20.1] FATAL: no build.gradle in %PROBE% & exit /b 3)
if not exist "%JAVA_HOME%\bin\java.exe" (echo [build-1.20.1] FATAL: JAVA_HOME is not a JDK & exit /b 2)

echo [build-1.20.1] JAVA_HOME        = %JAVA_HOME%
echo [build-1.20.1] GRADLE_USER_HOME = %GRADLE_USER_HOME%
echo [build-1.20.1] project          = %PROBE%
echo [build-1.20.1] tasks            = %TASKS%
if defined NODAEMON echo [build-1.20.1] daemon           = disabled
if not defined NODAEMON echo [build-1.20.1] daemon           = default
echo [build-1.20.1] log              = %LOG%
echo [build-1.20.1] command          = "%GRADLE_BIN%" --init-script "%INIT%" -p "%PROBE%" --no-watch-fs %NODAEMON% %TASKS%

rem ---- 127.0.0.1:8899 mirrors maven.neoforged.net; warn only ----
powershell -NoProfile -Command "try { $c=New-Object Net.Sockets.TcpClient; $c.Connect('127.0.0.1',8899); $c.Close(); exit 0 } catch { exit 1 }"
if errorlevel 1 echo [build-1.20.1] WARNING: 127.0.0.1:8899 not listening - dependency download will fail

rem ---- preflight: a java child must be able to write inside the project ----
rem DSH provisions write access per workspace. If this workspace was never
rem provisioned, java children get AccessDenied and gradle cannot create
rem .gradle/ or build/ no matter which daemon flags are used.
powershell -NoProfile -Command "try { $d = Join-Path $env:PROBE '.build-preflight'; New-Item -ItemType Directory -Path $d -Force -ErrorAction Stop | Out-Null; Remove-Item $d -Recurse -Force -ErrorAction SilentlyContinue; exit 0 } catch { exit 1 }"
if errorlevel 1 (echo [build-1.20.1] FATAL: this process cannot write inside %PROBE% & exit /b 4)
if exist "%~dp0tools\Preflight.java" "%JAVA_HOME%\bin\java.exe" "%~dp0tools\Preflight.java" "%PROBE%"
if errorlevel 9 (echo [build-1.20.1] FATAL: java child cannot write inside %PROBE% -- this workspace has no DSH sandbox grant. & echo [build-1.20.1] Fix: run the diagnose-windows-sandbox-acl skill script unconstrained, or point FARMSTORM_PROBE at a provisioned directory. & exit /b 4)

if defined DRY (echo [build-1.20.1] --dry: not executing & goto done)

call "%GRADLE_BIN%" --init-script "%INIT%" -p "%PROBE%" --no-watch-fs %NODAEMON% %TASKS% > "%LOG%" 2>&1
set "RC=%ERRORLEVEL%"
powershell -NoProfile -Command "if (Test-Path -LiteralPath '%LOG%') { Get-Content -LiteralPath '%LOG%' -Tail 20 }"

:done
if not defined RC set "RC=0"
echo [build-1.20.1] exit code: %RC%
endlocal & exit /b %RC%
