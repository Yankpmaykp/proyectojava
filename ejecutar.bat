@echo off
setlocal
cd /d "%~dp0"
chcp 65001 >nul
call "%~dp0compilar.bat" || exit /b 1
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstdin.encoding=UTF-8 -cp "out\main;lib\mysql-connector-j-9.1.0.jar" hoteles.Main
