#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
# $Id: optit.sh,v 1.1 2005-04-15 20:08:23 kohsuke Exp $

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
