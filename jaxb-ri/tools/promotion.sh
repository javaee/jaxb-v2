#!/bin/bash

if [ "$#" -eq 1 ]; then
    CURRENT_VERSION=$1
fi

if [ "$#" -eq 0 ]; then
    echo "No version specified, reading release version from pom file"
    CURRENT_VERSION=`cat pom.xml | grep '<version' -m 1 | cut -d ">" -f 2 | cut -d "<" -f 1 | cut -d "-" -f 1`
fi

echo "Major release version found: $CURRENT_VERSION"  

SCRIPT_DIR=$(cd $(dirname $0); pwd -P)

cd $SCRIPT_DIR/../.. || {
	echo >&2 "Cannot change to top of GIT working directory"
	exit 1
}

command -v git > /dev/null 2>&1 || {
	echo >&2 "Cannot locate git executable"
	exit 1
}

GIT=$(command -v git 2>&1)

DATESTAMP=`date +%y%m%d.%H%M`
BUILD_NUMBER=b${DATESTAMP}
DEVELOPER_VERSION=${CURRENT_VERSION}-SNAPSHOT
RELEASE_QUALIFIER=${BUILD_NUMBER}
RELEASE_VERSION=${CURRENT_VERSION}-${RELEASE_QUALIFIER}
RELEASE_TAG=${RELEASE_VERSION}

check_set_pom_file()
{
    if [ ! -f jaxb-ri/tools/set_pom_version.sh ]; then
        echo "tools/set_pom_version.sh file not found!"
        exit 1
    fi
}

tagging()
{
	echo -n "Preparing tag ${RELEASE_TAG}..."
	${GIT} tag -m "Tagging for Release ${RELEASE_VERSION}" ${RELEASE_TAG}
	if [ $? -ne 0 ]; then
		echo "FAILED."
		echo "git tag failed: $!"
		exit 1
	fi
	echo "DONE."
}


push_tag()
{
	echo -n "Pushing tag..."
	${GIT} push origin ${RELEASE_TAG}
	if [ $? -ne 0 ]; then
		${GIT} pull --rebase
		${GIT} push
		if [ $? -ne 0 ]; then
			echo "FAILED."
			echo "git push failed: $!"
			exit 1
		fi
	fi
	echo "DONE."
}

git_clean()
{
    ${GIT} clean -d -f -x
}

set_pom_version()
{
    ./jaxb-ri/tools/set_pom_version.sh $DEVELOPER_VERSION $RELEASE_VERSION
    if [ $? -ne 0 ]; then
        echo "FAILED."
        echo "Setting version to poms failed: $!"
        exit 1
    fi
}

echo "Doing JAXB promotion"
echo "DEVELOPER_VERSION = ${DEVELOPER_VERSION}"
echo "RELEASE_VERSION = ${RELEASE_VERSION}"

check_set_pom_file
git_clean
tagging
set_pom_version
push_tag

exit 0
