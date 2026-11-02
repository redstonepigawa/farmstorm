@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   Farmstorm v0.1.1  -  启动开发客户端
echo ============================================
echo.
echo 这会用 1.21.1 + NeoForge 21.1.213 起一个已加载 Farmstorm 的客户端，
echo 不用自己装 1.21.1、也不用把 jar 放进 mods。
echo.
echo 进游戏后：创造模式物品栏最后多一个「农场风云」标签页，22 个物品都在里面。
echo.
call ".tools\gw.cmd" runClient
echo.
echo 客户端已退出。
pause
