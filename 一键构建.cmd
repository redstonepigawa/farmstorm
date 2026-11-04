@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   Farmstorm v0.2.1  -  一键构建
echo ============================================
echo.
call ".tools\gw.cmd" build
if errorlevel 1 goto fail
if not exist "dist" mkdir "dist"
copy /y "build\libs\farmstorm-0.2.1+mc1.21.1.jar" "dist\" >nul
copy /y "build\libs\farmstorm-0.2.1+mc1.21.1-sources.jar" "dist\" >nul
echo.
echo [OK] 构建成功，jar 已同步到 dist\
pause
exit /b 0
:fail
echo.
echo [FAIL] 构建失败，请看上面第一条 error。
pause
exit /b 1
