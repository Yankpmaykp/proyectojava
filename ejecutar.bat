@echo off
rem Ejecuta la aplicacion de consola (compilar antes con compilar.bat)
chcp 65001 > nul
java -Dstdout.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out\main hoteles.Main
