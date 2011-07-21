#!/bin/bash

export LSC_HOME=$( cd `dirname $0` && pwd )

export JAVA_OPTIONS="-Dorg.mortbay.jetty.webapp.parentLoaderPriority=true -DLSC_HOME=$LSC_HOME"

if ! [ ! $DEBUG ] ; then
    export JAVA_OPTIONS="$JAVA_OPTIONS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"
fi

if ! test -d $LSC_HOME/jetty/logs && ! test -d $LSC_HOME/logs ; then
   mkdir $LSC_HOME/logs || exit $?
   Ln -s $LSC_HOME/logs $LSC_HOME/jetty/logs || exit $?
elif test -d $LSC_HOME/jetty/logs && ! test -h $LSC_HOME/jetty/logs && ! test -f $LSC_HOME/logs ; then
   mv $LSC_HOME/jetty/logs $LSC_HOME/logs || exit $?
   ln -s $LSC_HOME/logs $LSC_HOME/jetty/logs || exit $?
fi

$LSC_HOME/jetty/bin/jetty.sh $1 $2 $3 $4 $5 $6 $7 $8 $9
