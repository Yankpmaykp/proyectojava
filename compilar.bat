@echo off
setlocal
cd /d "%~dp0"
if not exist out\main mkdir out\main
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.1.0.jar" -d out\main -sourcepath src src\hoteles\Main.java src\hoteles\util\EjecutorScript.java
if errorlevel 1 (echo Error de compilacion & exit /b 1)
echo Compilado en out\main