@echo off
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\javac.exe" (
        set JAVAC_CMD="C:\Program Files\Apache NetBeans\jdk\bin\javac.exe"
    )
)

set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
        set JAVA_CMD="C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
    )
)

if not exist "bin" mkdir bin
echo [1/2] Compilando clases...
%JAVAC_CMD% -encoding ISO-8859-1 -cp ".;src;src\especificacion\java-cup-11b-runtime.jar" -d bin src\ve\edu\unet\*.java src\ve\edu\unet\nodosAST\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Fallo la compilacion.
    pause
    exit /b %errorlevel%
)

echo [2/2] Ejecutando prueba de Backend...
echo.
%JAVA_CMD% -cp "bin;src\especificacion\java-cup-11b-runtime.jar" ve.edu.unet.TestBackend
echo.
pause
