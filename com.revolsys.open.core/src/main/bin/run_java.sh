#!/bin/sh
BIN_DIR=`dirname $0`
APP_HOME=$(readlink -f $BIN_DIR/..)
                               
LOCALCLASSPATH="$APP_HOME/config:$LOCALCLASSPATH"

DIRLIBS="${APP_HOME}/lib/*.jar"
for i in ${DIRLIBS}
do
    if [ "$i" != "${DIRLIBS}" ] ; then
      LOCALCLASSPATH=$LOCALCLASSPATH:"$i"
    fi
done

DIRLIBS="${APP_HOME}/lib/*.zip"
for i in ${DIRLIBS}
do
    if [ "$i" != "${DIRLIBS}" ] ; then
      LOCALCLASSPATH=$LOCALCLASSPATH:"$i"
    fi
done

exec java -cp $LOCALCLASSPATH $*
