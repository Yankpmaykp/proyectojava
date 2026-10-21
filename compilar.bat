@echo off
rem Compila el proyecto en out\main (aplicacion) y out\test (pruebas)
set JUNIT=lib\junit-platform-console-standalone-1.10.2.jar
if exist out rmdir /s /q out
mkdir out\main
mkdir out\test
javac -encoding UTF-8 -d out\main src\hoteles\*.java src\hoteles\modelo\*.java src\hoteles\vista\*.java src\hoteles\controlador\*.java || exit /b 1
javac -encoding UTF-8 -cp "out\main;%JUNIT%" -d out\test test\hoteles\modelo\*.java || exit /b 1
echo Compilacion correcta.
