#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

if [ "$#" -eq 2 ]; then
    DEVELOPER_VERSION=$1
    RELEASE_VERSION=$2
fi

if [ "$#" -eq 1 ]; then
    CURRENT_VERSION=$1

    echo "No version specified, reading release version from pom file"
    CURRENT_VERSION=`cat pom.xml | grep '<version' -m 1 | cut -d ">" -f 2 | cut -d "<" -f 1 | cut -d "-" -f 1`

    echo "Major release version found: $CURRENT_VERSION"

    DATESTAMP=`date +%y%m%d.%H%M`
    BUILD_NUMBER=b${DATESTAMP}
    DEVELOPER_VERSION=${CURRENT_VERSION}-SNAPSHOT
    RELEASE_QUALIFIER=${BUILD_NUMBER}
    RELEASE_VERSION=${CURRENT_VERSION}-${RELEASE_QUALIFIER}
fi

SCRIPT_DIR=$(cd $(dirname $0); pwd -P)

cd $SCRIPT_DIR/../.. || {
    echo >&2 "Cannot change to top working directory"
    exit 1
}

command -v git > /dev/null 2>&1 || {
    echo >&2 "Cannot locate git executable"
    exit 1
}

GIT=$(command -v git 2>&1)

cleanup()
{
	${GIT} clean -d -f -x
	exit 1
}

edit_poms()
{
	TMPFILE=`mktemp $TMPDIR/${RELEASE_VERSION}.XXXXXXXX`
	find \
		$SCRIPT_DIR/../ \
		-name pom.xml \
		>> $TMPFILE

	echo "Updating pom files to have release versions ..."
	while read line
	do
		echo -n "Editing $line..."
		perl -i -pe "s|${DEVELOPER_VERSION}|${RELEASE_VERSION}|g" $line
		if [ $? -ne 0 ]; then
			echo "FAILED."
			echo "Replace versions failed for $line: $!"
			cleanup
		fi
		echo "DONE."
	done < "$TMPFILE"
	echo "DONE."
}

#############

echo "Updating JAXB POM Files"
echo "DEVELOPER_VERSION = ${DEVELOPER_VERSION}"
echo "RELEASE_VERSION = ${RELEASE_VERSION}"

edit_poms

exit 0
