#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
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
    
    cd "`dirname $PRG`"
    
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
    
    cd "$saveddir"
fi

[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    JAXB_HOME=`cygpath -w "$JAXB_HOME"`
}

if [ -n "$JAVA_HOME" ]
then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

#JXC module path
JAXB_PATH=${JAXB_HOME}/mod/jaxb-xjc.jar:\
${JAXB_HOME}/mod/jaxb-api.jar:\
${JAXB_HOME}/mod/codemodel.jar:\
${JAXB_HOME}/mod/jaxb-runtime.jar:\
${JAXB_HOME}/mod/istack-commons-runtime.jar:\
${JAXB_HOME}/mod/istack-commons-tools.jar:\
${JAXB_HOME}/mod/rngom.jar:\
${JAXB_HOME}/mod/xsom.jar:\
${JAXB_HOME}/mod/dtd-parser.jar:\
${JAXB_HOME}/mod/txw2.jar:\
${JAXB_HOME}/mod/stax-ex.jar:\
${JAXB_HOME}/mod/FastInfoset.jar:\
${JAXB_HOME}/mod/javax.activation.jar:\
${JAXB_HOME}/mod/relaxng-datatype.jar


JAVA_VERSION=`${JAVA} -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed -E 's/^(1\.)?([0-9]+).+$/\2/'`
echo "Java major version: ${JAVA_VERSION}"

# Check if supports module path
if [[ ${JAVA_VERSION} -lt 9 ]] ;
then
  #classpath
  exec "${JAVA}" -cp "${JAXB_PATH}" ${XJC_OPTS} com.sun.tools.xjc.XJCFacade "$@"
elif [[ ${JAVA_VERSION} -ge 9 && ${JAVA_VERSION} -le 10 ]] ;
then
  #module path + upgrade
  exec "${JAVA}" --module-path "${JAXB_PATH}" --upgrade-module-path ${JAXB_HOME}/mod/jaxb-api.jar ${XJC_OPTS} -m com.sun.tools.xjc/com.sun.tools.xjc.XJCFacade "$@"
else
  #module path
  exec "${JAVA}" --module-path "${JAXB_PATH}" ${XJC_OPTS} -m com.sun.tools.xjc/com.sun.tools.xjc.XJCFacade "$@"
fi


