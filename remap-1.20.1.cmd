@echo off
rem ============================================================
rem  Farmstorm 1.20.1 - reobfuscate the built jar into the
rem  NeoForge/Forge 1.20.1 production namespace
rem  (official class names + SRG member names m_12345_ / f_12345_).
rem
rem  Usage:
rem    remap-1.20.1.cmd                       remap build\libs default jar
rem    remap-1.20.1.cmd -Force                rebuild the mapping as well
rem    remap-1.20.1.cmd -InputJar a.jar -OutputJar b.jar
rem    remap-1.20.1.cmd -DistJar dist\farmstorm-0.2.4+mc1.20.1.jar
rem ============================================================
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\remap-to-srg.ps1" %*
set "RC=%ERRORLEVEL%"
echo [remap-1.20.1] exit code: %RC%
endlocal & exit /b %RC%
