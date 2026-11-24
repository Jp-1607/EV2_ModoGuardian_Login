@echo off
setlocal
cd /d "%~dp0" || exit /b 1
where node >nul 2>&1
if errorlevel 1 goto sin_node
echo En el primer inicio se muestran las cuentas y claves ficticias.
echo Guarda las claves y deja esta ventana abierta mientras usas la app.
node api/servidor.mjs
set "codigo=%errorlevel%"
pause
exit /b %codigo%

:sin_node
echo Instala Node.js 22 o superior y abre de nuevo este archivo.
pause
exit /b 1
