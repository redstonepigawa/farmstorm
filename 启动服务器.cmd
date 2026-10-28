@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   Farmstorm v0.0.1  -  启动专用服务器
echo ============================================
echo.
echo 进服后输入中文看不到？看 run\logs\latest.log。
echo 停服请在另一个窗口执行： taskkill /F /IM java.exe
echo.
call ".tools\gw.cmd" runServer
pause
