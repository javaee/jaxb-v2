#!/bin/sh
#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the "License").  You may not use this file except
# in compliance with the License.
# 
# You can obtain a copy of the license at
# https://jwsdp.dev.java.net/CDDLv1.0.html
# See the License for the specific language governing
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# HEADER in each file and include the License file at
# https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
# add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your
# own identifying information: Portions Copyright [yyyy]
# [name of copyright owner]
#

# Script to run XJC
#

# Resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

WEBSERVICES_LIB=$PRG/../../..

# TODO: figure out where to find jsr173_1.0_api.jar and activation.jar

# Set CLASSPATH
CLASSPATH=`tr '\n' ':' <<EOF
$WEBSERVICES_LIB/jaxb/lib/jaxb-api.jar
$WEBSERVICES_LIB/jaxb/lib/jaxb-xjc.jar
$WEBSERVICES_LIB/jaxb/lib/jaxb-impl.jar
$WEBSERVICES_LIB/jaxb/lib/jaxb1-impl.jar
EOF`

if [ -n "$JAVA_HOME" ]
then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA=java
fi
 
$JAVA $XJC_OPTS -cp "$CLASSPATH" com.sun.tools.xjc.Driver "$@"
