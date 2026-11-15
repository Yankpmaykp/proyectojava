@echo off
rem Crea la base de datos "hoteles" (tablas, procedimientos, usuario de la aplicacion) y carga datos de ejemplo.
rem Pide dos veces la contrasena del usuario root de MySQL. Ajuste la ruta si MySQL esta en otro sitio.
setlocal
cd /d "%~dp0"
set MYSQL="C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"
if not exist %MYSQL% set MYSQL=mysql
%MYSQL% -u root -p --default-character-set=utf8mb4 < sql\01_schema.sql
if errorlevel 1 (echo Error al crear la base de datos & exit /b 1)
%MYSQL% -u root -p --default-character-set=utf8mb4 < sql\02_datos_demo.sql
if errorlevel 1 (echo Error al cargar los datos de ejemplo & exit /b 1)
echo Base de datos creada.
