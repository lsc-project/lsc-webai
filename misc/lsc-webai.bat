@echo off

set JDWP_PORT=8000

if "%OS%" == "Windows_NT" setlocal
set CURRENT_DIR=%cd%
set JAVA_OPTIONS=-Xmx1024m -Dfile.encoding=UTF-8 -Dorg.mortbay.jetty.webapp.parentLoaderPriority=true -DLSC_HOME="%CURRENT_DIR%"

if "%DEBUG%" == "1" set JAVA_OPTIONS="%JAVA_OPTIONS% -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=%JDWP_PORT%"

set _EXECJAVA=start java


cd "%CURRENT_DIR%\jetty"
mkdir logs
echo JAVA_OPTIONS=%JAVA_OPTIONS%
%_EXECJAVA%  %JAVA_OPTIONS%  -jar start.jar
