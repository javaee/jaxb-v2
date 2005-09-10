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

