@echo off
setlocal DisableDelayedExpansion
cd /d "%~dp0" || exit /b 1
where node >nul 2>&1
if errorlevel 1 goto sin_node
if exist "ACCESOS-DE-PRUEBA.txt" set /p INSERTCODE_CLAVE_DEMO=<"ACCESOS-DE-PRUEBA.txt"
if defined INSERTCODE_CLAVE_DEMO if not defined INSERTCODE_DATOS set "INSERTCODE_DATOS=api/datos/evaluacion-3.json"
echo En el primer inicio se muestran las cuentas y claves ficticias.
echo Guarda las claves y deja esta ventana abierta mientras usas la app.
node api/servidor.mjs --demo-google
set "codigo=%errorlevel%"
pause
exit /b %codigo%

:sin_node
echo Instala Node.js 22 o superior y abre de nuevo este archivo.
pause
exit /b 1
