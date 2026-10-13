@echo off
echo Generando Lexico con JFlex...
java -jar src\especificacion\jflex-full-1.8.2.jar -d src\ve\edu\unet src\especificacion\lexico.flex

echo Generando Parser con CUP...
java -jar src\especificacion\java-cup-11b.jar -destdir src\ve\edu\unet -parser parser -symbols sym src\especificacion\sintactico.cup
if exist src\ve\edu\unet\Lexico.java~ del /f /q src\ve\edu\unet\Lexico.java~

echo Listo! Analizadores regenerados.
pause
