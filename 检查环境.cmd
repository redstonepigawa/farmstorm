@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ============================================
echo   Farmstorm v0.2.7  -  验收自检
echo ============================================
echo.
echo --- 1) 静态验收（注册名 / 配置键 / 语言键 / 禁令扫描）---
set "PY=C:\Users\DASHpig\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe"
set "PYTHONIOENCODING=utf-8"
"%PY%" -X utf8 "tools\verify_project.py"
echo.
echo --- 2) 产物计数 ---
if exist "build\libs\farmstorm-0.2.7+mc1.21.1.jar" (echo jar: OK) else (echo jar: 缺失，先跑 一键构建.cmd)
pause
