@echo off
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
        set JAVA_CMD="C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
    )
)
echo Generando Lexico con JFlex...
%JAVA_CMD% -jar src\especificacion\jflex-full-1.8.2.jar -d src\ve\edu\unet src\especificacion\lexico.flex

echo Generando Parser con CUP...
%JAVA_CMD% -jar src\especificacion\java-cup-11b.jar -destdir src\ve\edu\unet -parser parser -symbols sym src\especificacion\sintactico.cup
if exist src\ve\edu\unet\Lexico.java~ del /f /q src\ve\edu\unet\Lexico.java~

echo Listo! Analizadores regenerados.
pause
