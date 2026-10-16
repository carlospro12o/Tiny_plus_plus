@echo off
title Servidor Tiny++ IDE - Integracion Compilador Java
echo ========================================================
echo  Iniciando Servidor Web y Puente de Compilacion Tiny++
echo ========================================================
echo.
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js no se encuentra en el PATH del sistema.
    echo Por favor instala Node.js para habilitar la ejecucion del servidor IDE.
    pause
    exit /b 1
)

if not exist "bin\ve\edu\unet\parser.class" (
    echo [INFO] Binarios Java no detectados en bin. Compilando compilador...
    call compilar_codigo.bat
)

start http://localhost:3000
node ide\server.js
pause
