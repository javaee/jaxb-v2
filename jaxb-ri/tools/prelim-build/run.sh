#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#


LANG=en_us
export LANG
cd jaxb-ri
rm log > /dev/null 2>&1

# fetch the latest file
cvs -z3 -q upd -Pd

# run the dist target
ant dist > log 2>&1

# check the result
if [ $? -ne 0 ]; then
  # failure!
  cat log | mail -s "[jaxb-pre-build] preliminary build failure" kohsuke.kawaguchi@sun.com
fi

