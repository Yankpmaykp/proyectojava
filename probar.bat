@echo off
rem Ejecuta las pruebas JUnit (compilar antes con compilar.bat)
set JUNIT=lib\junit-platform-console-standalone-1.10.2.jar
java -jar %JUNIT% execute -cp "out\main;out\test" --scan-classpath out\test --disable-ansi-colors --details=tree
