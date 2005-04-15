#!/bin/sh
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



if [ -n "$CLASSPATH" ] ; then
  LOCALCLASSPATH="$CLASSPATH"
fi

# add the jar files
for i in "${JAXB_HOME}"/lib/*.jar
do
    if [ -z "$LOCALCLASSPATH" ] ; then
        LOCALCLASSPATH=$i
    else
        LOCALCLASSPATH="$i":"$LOCALCLASSPATH"
    fi
done

[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    LOCALCLASSPATH=`cygpath -w -p ${LOCALCLASSPATH}`
}

if [ -n "$JAVA_HOME" ]
then
    JAVA="$JAVA_HOME"/bin/java
else
    JAVA=java
fi

exec "$JAVA" $XJC_OPTS -classpath "$LOCALCLASSPATH" com.sun.tools.xjc.Driver "$@"
