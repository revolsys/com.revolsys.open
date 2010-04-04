@echo off
set APP_HOME=%~dp0..

set LOCALCLASSPATH=%APP_HOME%\conf;%LOCALCLASSPATH%

for %%i in ("%APP_HOME%\lib\*.jar") do call %APP_HOME%\bin\lcp.bat %%i
for %%i in ("%APP_HOME%\lib\*.zip") do call %APP_HOME%\bin\lcp.bat %%i

java -cp %LOCALCLASSPATH% %*
