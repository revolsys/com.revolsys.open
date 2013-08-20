@echo off
set APP_HOME=%~dp0..

set JAVA_OPTS=
if exist %APP_HOME%\etc\java_config.bat call %APP_HOME%\etc\java_config.bat 

for %%i in ("%APP_HOME%\lib\*.jar") do call "%APP_HOME%\bin\lcp.bat" %%i
for %%i in ("%APP_HOME%\lib\*.zip") do call "%APP_HOME%\bin\lcp.bat" %%i

java %JAVA_OPTS% -cp "%LOCALCLASSPATH%" %*
