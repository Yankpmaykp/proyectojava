@echo off
rem Pruebas JUnit contra MySQL (usa la base hoteles_test, que recrea). Para otro servidor: probar.bat localhost:3307
setlocal
cd /d "%~dp0"
chcp 65001 >nul
call "%~dp0compilar.bat" || exit /b 1
if not exist out\test mkdir out\test
javac -encoding UTF-8 -cp "out\main;lib\mysql-connector-j-9.1.0.jar;lib\junit-platform-console-standalone-1.10.2.jar" -d out\test test\hoteles\modelo\*.java
if errorlevel 1 (echo Error de compilacion de las pruebas & exit /b 1)
set HOST=%1
if "%HOST%"=="" set HOST=localhost:3306
java -Dtest.db.host=%HOST% -jar lib\junit-platform-console-standalone-1.10.2.jar execute -cp "out\main;out\test;lib\mysql-connector-j-9.1.0.jar" --scan-classpath out\test --disable-banner
