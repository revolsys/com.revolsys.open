@echo off
set APP_HOME=%~dp0..

set LOCALCLASSPATH=%APP_HOME%\scripts
call %APP_HOME%\bin\run_java.bat com.revolsys.parallel.tools.ProcessorPipelineTool %*
