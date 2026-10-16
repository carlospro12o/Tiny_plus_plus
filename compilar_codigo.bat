@echo off
if not exist "bin" mkdir bin
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\javac.exe" (
        set JAVAC_CMD="C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\javac.exe"
    ) else if exist "C:\Program Files\Apache NetBeans\jdk\bin\javac.exe" (
        set JAVAC_CMD="C:\Program Files\Apache NetBeans\jdk\bin\javac.exe"
    )
)
echo Compilando clases Java con %JAVAC_CMD%...
%JAVAC_CMD% -encoding UTF-8 -cp ".;src;src\especificacion\java-cup-11b-runtime.jar" -d bin src\ve\edu\unet\*.java src\ve\edu\unet\nodosAST\*.java
if %errorlevel% equ 0 (
    echo [OK] Compilacion completada con exito en carpeta bin.
) else (
    echo [ERROR] Hubo un error en la compilacion.
)
pause
