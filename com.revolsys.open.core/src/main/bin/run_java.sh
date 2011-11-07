#!/bin/sh

APP_HOME=`dirname $0`/..
                               
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
