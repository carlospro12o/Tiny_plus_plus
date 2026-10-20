@echo off
title Servidor Tiny++ IDE - Integracion Compilador Java
echo ========================================================
echo  Iniciando Servidor Web y Puente de Compilacion Tiny++
echo ========================================================
echo.
set NODE_CMD=node
where node >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\nodejs\node.exe" (
        set NODE_CMD="C:\Program Files\nodejs\node.exe"
    ) else if exist "C:\Program Files (x86)\nodejs\node.exe" (
        set NODE_CMD="C:\Program Files (x86)\nodejs\node.exe"
    ) else if exist "%LOCALAPPDATA%\Programs\nodejs\node.exe" (
        set NODE_CMD="%LOCALAPPDATA%\Programs\nodejs\node.exe"
    ) else (
        echo [ERROR] Node.js no se encuentra en el PATH ni en las rutas estandar.
        echo Por favor instala Node.js desde https://nodejs.org/ para usar el servidor.
        pause
        exit /b 1
    )
)

echo Usando Node.js: %NODE_CMD%
start http://localhost:3000
%NODE_CMD% ide\server.js
pause
