@echo off
set ARCHIVO=%1
if "%ARCHIVO%"=="" set ARCHIVO=ejemplos\factorial_tiny_plus.tny
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" (
        set JAVA_CMD="C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe"
    ) else if exist "C:\Program Files\Apache NetBeans\jdk\bin\java.exe" (
        set JAVA_CMD="C:\Program Files\Apache NetBeans\jdk\bin\java.exe"
    )
)
echo Ejecutando compilador sobre: %ARCHIVO%
%JAVA_CMD% -cp "bin;src\especificacion\java-cup-11b-runtime.jar" ve.edu.unet.parser %ARCHIVO%
pause
