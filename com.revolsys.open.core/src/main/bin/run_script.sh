#!/bin/sh
BIN_DIR=`dirname $0`
APP_HOME=$(readlink -f $BIN_DIR/..)

export LOCALCLASSPATH=$APP_HOME/etc:$APP_HOME/scripts
export JAVA_OPTS=
if [ -f "$APP_HOME/etc/java_config.sh" ]; then
  . "$APP_HOME/etc/java_config.sh"
fi

exec "$BIN_DIR/run_java.sh" $JAVA_OPTS com.revolsys.parallel.tools.ScriptTool "-DapplicationHome=$APP_HOME" -s "$@"
