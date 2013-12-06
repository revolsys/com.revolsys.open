@echo off
set BIN_DIR=%~dp0
set APP_HOME=%BIN_DIR%..

set LOCALCLASSPATH=%APP_HOME%\etc;%APP_HOME%\scripts

call "%BIN_DIR%\run_java.bat" %JAVA_OPTS% com.revolsys.parallel.tools.ScriptTool "-DapplicationHome=%APP_HOME%" -s %*
