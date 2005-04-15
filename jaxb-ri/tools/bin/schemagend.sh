#!/bin/sh +x
#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

#
# Make sure that JAXB_HOME and JAVA_HOME are set
#
if [ -z "$JAXB_HOME" ]
then
    # search the installation directory
    
    PRG=$0
    progname=`basename $0`
    saveddir=`pwd`
    
    cd `dirname $PRG`
    
    while [ -h "$PRG" ] ; do
        ls=`ls -ld "$PRG"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '.*/.*' > /dev/null; then
            PRG="$link"
        else
            PRG="`dirname $PRG`/$link"
        fi
    done
    
    JAXB_HOME=`dirname "$PRG"`/..
    
    # make it fully qualified
    cd "$saveddir"
    JAXB_HOME=`cd "$JAXB_HOME" && pwd`
    
    cd $saveddir
fi


if [ -n "$JAVA_HOME" ]
then
    APT="$JAVA_HOME"/bin/apt
else
    APT=apt
fi

APT_OPTS="-J-Xdebug -J-Xnoagent -J-Djava.compiler=NONE -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000'"

if [ `expr \`uname\` : 'CYGWIN'` -eq 6 ]
then
    JAXB_HOME="`cygpath -w "$JAXB_HOME"`"
fi


exec "$APT" $APT_OPTS $XJC_OPTS -factorypath "$JAXB_HOME"/dist/lib/jaxb-xjc.jar -factory com.sun.tools.jxc.apt.SchemaGenerator -nocompile "$@"
