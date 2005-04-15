#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

cd $JAXB_HOME
find . -name "*.java" | \
xargs grep "3RD PARTY CODE"
