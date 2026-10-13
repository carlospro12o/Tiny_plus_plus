@echo off
if not exist "bin" mkdir bin
echo Compilando clases Java...
javac -encoding UTF-8 -cp ".;src;src\especificacion\java-cup-11b-runtime.jar" -d bin src\ve\edu\unet\*.java src\ve\edu\unet\nodosAST\*.java
if %errorlevel% equ 0 (
    echo [OK] Compilacion completada con exito en carpeta bin.
) else (
    echo [ERROR] Hubo un error en la compilacion.
)
pause
