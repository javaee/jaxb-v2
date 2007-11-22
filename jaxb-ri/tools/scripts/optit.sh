#!/bin/sh

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License. You can obtain
# a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
# or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
# 
# When distributing the software, include this License Header Notice in each
# file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
# Sun designates this particular file as subject to the "Classpath" exception
# as provided by Sun in the GPL Version 2 section of the License file that
# accompanied this code.  If applicable, add the following below the License
# Header, with the fields enclosed by brackets [] replaced by your own
# identifying information: "Portions Copyrighted [year]
# [name of copyright owner]"
# 
# Contributor(s):
# 
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
# Usage:
#   optit.sh <JVMopts>/<OptItOpts> <classname> <args> ...
# 
# This shell script can be used to launch a Java program with OptimizeIt.
# To use it,
#    1. launch your Java program with this.
#    2. open OptimizeIt GUI tool
#    3. wait for the program completion
#    4. attach OptimizeIt to the running session from the program menu.
#    5. stop the CPU profiler to obtain the profiling result
#
# This tool is primarily designed to be used in conjunction with the
# PerformanceTestRunner program (in test/src), so that one can manually
# investigate a particular performance measurement scenario deeper.
#
# Combined with setenv.sh, this shell script lets you launch OptimizeIt
# without going through a tedious classpath setting dialog of OptimizeIt.
#
# $Id: optit.sh,v 1.3 2007-11-22 00:53:37 kohsuke Exp $

if [ "$OPTIMIZEIT_HOME" = "" ]; then
  echo the OPTIMIZEIT_HOME env var has to be set.
  exit 1
fi

# OS detection
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

export CLASSPATH=$CLASSPATH:$OPTIMIZEIT_HOME/lib/optit.jar
export PATH="$PATH:$OPTIMIZEIT_HOME/lib"

oibcp_path=$OPTIMIZEIT_HOME/lib/oibcp.jar

if [ "$1" = "PerformanceTestRunner" ]; then
  # Having the -enableAPI switch prevents the profiler from auto-start,
  # so this is inconvenient when you want to profile the whole program.
  # But unless this option, OptimizeIt cannot be controlled from within
  # the profiled program.
  extraOpts=-enableAPI
fi

# for options see the OptimizeIt manual
# FIXME: add a link to the online document.
# FIXME: allow caller to specify JVM settings.
java \
  -server \
  -verbose:gc \
  -Xmx1024m -Xms1024m \
  -Xboundthreads \
  -Xrunpri \
  -Xnoclassgc \
  -Djava.compiler=NONE \
  -Xbootclasspath/a:$oibcp_path \
  intuitive.audit.Audit \
  -startCPUprofiler:type=instrumentation \
  -noexit \
  $extraOpts \
  "$@"

#  -dmp \
