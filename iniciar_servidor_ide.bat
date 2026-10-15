@echo off
title Servidor Tiny++ IDE - Integracion Compilador Java
echo ========================================================
echo  Iniciando Servidor Web y Puente de Compilacion Tiny++
echo ========================================================
echo.
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js no se encuentra en el PATH del sistema.
    echo Por favor instala Node.js para habilitar la compilacion automatica en 1 clic.
    pause
    exit /b 1
)

start http://localhost:3000
node ide\server.js
pause
