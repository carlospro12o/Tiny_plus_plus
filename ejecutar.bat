@echo off
set ARCHIVO=%1
if "%ARCHIVO%"=="" set ARCHIVO=ejemplos\factorial.tny
echo Ejecutando compilador sobre: %ARCHIVO%
java -cp "bin;src\especificacion\java-cup-11b-runtime.jar" ve.edu.unet.parser %ARCHIVO%
pause
