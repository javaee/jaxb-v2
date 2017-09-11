#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

while getopts :r:l:t:s:b:m:w:dh arg
do
    case "$arg" in
        r)  RELEASE_VERSION="${OPTARG:?}" ;;
        t)  GIT_VERSION="${OPTARG:?}" ;;
        l)  MAVEN_USER_HOME="${OPTARG:?}" ;;
        s)  MAVEN_SETTINGS="${OPTARG:?}" ;;
        m)  SOURCES_VERSION="${OPTARG:?}" ;;
        w)  WORKROOT="${OPTARG:?}" ;;
        u)  WWW_SVN_USER="${OPTARG:?}" ;;
        p)  WWW_SVN_PASSWORD="${OPTARG:?}" ;;
        d)  debug=true ;;
        h)
            echo "Usage: release.sh [-r RELEASE_VERSION] --mandatory, the release version string, for example 2.2.11"
            echo "                   [-t GIT_VERSION] --mandatory, git tag or sha to checkout for the release"
            echo "                   [-l MVEN_USER_HOME] -- optional, alternative maven local repository location"
            echo "                   [-w WORKROOT] -- optional, default is current dir (`pwd`)"
            echo "                   [-m SOURCES_VERSION] -- optional, version in pom.xml need to be repaced with \$RELEASE_VERSION, default is \${RELEASE_VERSION}-SNAPSHOT"
            echo "                   [-s MAVEN_SETTINGS] --optional, alternative maven settings.xml"
            echo "                   [-u WWW_SVN_USER] --optional, the svn scm username for commit the www docs, if not specified it uses cached credential"
            echo "                   [-p WWW_SVN_PASSWORD] --optional the svn scm password for commit the www docs"
            echo "                   [-d] -- debug mode"
            exit ;;
        "?")
            echo "ERROR: unknown option \"$OPTARG\"" 1>&2
            echo "" 1>&2 ;;
    esac
done

if [ "$M2_HOME" = "" -o ! -d $M2_HOME ]; then
    echo "ERROR: Check your M2_HOME: $M2_HOME"
    exit 1
fi

if [ "$JAVA_HOME" = "" -o ! -d $JAVA_HOME ]; then
    echo "ERROR: Check your JAVA_HOME: $JAVA_HOME"
    exit 1
fi
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
export TMPDIR=${TMPDIR:-/tmp}

PROXYURL=www-proxy.uk.oracle.com
PROXYPORT=80

export http_proxy=$PROXYURL:$PROXYPORT
export https_proxy=$http_proxy


export MAVEN_OPTS="-Xms256m -Xmx768m -XX:PermSize=256m -XX:MaxPermSize=512m -Dhttp.proxyHost=$PROXYURL -Dhttp.proxyPort=$PROXYPORT -Dhttps.proxyHost=$PROXYURL -Dhttps.proxyPort=$PROXYPORT"

if [ "$MAVEN_USER_HOME" = "" ]; then
     user=${LOGNAME:-${USER-"`whoami`"}}
     MAVEN_USER_HOME="/scratch/$user/.m2/repository"
fi

if [ -n "$MAVEN_SETTINGS" ]; then
    MAVEN_SETTINGS="-s $MAVEN_SETTINGS"
fi

if [ "$WORKROOT" = "" ]; then
    WORKROOT=`pwd`
fi

if [ "$MAVEN_USER_HOME" = "" ]; then
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${WORKROOT}/.m2/repository"
else
    MAVEN_LOCAL_REPO="-Dmaven.repo.local=${MAVEN_USER_HOME}"
fi

if [ "$RELEASE_VERSION" = "" ]; then
    echo "ERROR: you need to give the -r with the release revision"
    exit 1
fi

echo "Release on git version: $GIT_VERSION"
if [ "$GIT_VERSION" = "" ]; then
   exit 1;
fi

cd $WORKROOT || {
    echo "ERROR: fail to cd to working dir $WORKROOT"
    exit 1
}

if [ -e jaxb-ri ] ; then
   echo "INFO: Removing old JAXB workspace"
   rm -rf jaxb-ri
fi

echo "INFO: Cloning jaxb-ri git repository"
git clone git@git@github.com:javaee/jaxb-v2.git || {
    echo "fail to clone the git repository"
    exit 1
}

# create release branch from the given git version to be released
cd jaxb-ri
RELEASE_BRANCH=`echo jaxb-${RELEASE_VERSION}-branch |sed 's/\./_/g'`
git checkout -b $RELEASE_BRANCH $GIT_VERSION || {
    echo "ERROR: fail to checkout $GIT_VRSION to branch $RELEASE_BRANCH"
    exit 1
}
GIT_SHA=`git rev-parse --short HEAD`
echo "INFO: release JAXB RI $RELEASE_VERSION base on git sha $GIT_SHA"
SOURCES_VERSION=${SOURCES_VERSION:-"${RELEASE_VERSION}-SNAPSHOT"}
echo "INFO: Replacing project version $SOURCES_VERSION in sources with new release version $RELEASE_VERSION"
./jaxb-ri/tools/set_pom_version.sh $SOURCES_VERSION $RELEASE_VERSION
git commit -a -m "release version $RELEASE_VERSION" || {
    echo "ERROR: fail to commit the modification of version in pom.xml"
    exit 1
}
  
if [ "$debug" = "true" ]; then
    echo "DEBUG: build while no deploy"
    echo "INFO: mvn $MAVEN_SETTINGS -C -B -f jaxb-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts clean install"
    mvn $MAVEN_SETTINGS -C -B -f jaxb-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts clean install
else
    echo "INFO: Build and Deploy ..."
    echo "INFO: mvn $MAVEN_SETTINGS -C -B -f jaxb-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts clean install deploy"
    mvn $MAVEN_SETTINGS -C -B -f jaxb-ri/pom.xml $MAVEN_LOCAL_REPO -DskipTests=true -Prelease-profile,release-sign-artifacts clean install deploy
fi
if [ $? -ne 0 ]; then
      exit 1
fi
echo "INFO: Tagging release $RELEASE_VERSION"
echo "INFO: git tag -m \"Tag release $RELEASE_VERSION\" $RELEASE_TAG"
RELEASE_TAG=`echo "jaxb-$RELEASE_VERSION" |sed 's/\./_/g'`
git tag -m "Tag release $RELEASE_VERSION" $RELEASE_TAG

if [ "$debug" = "true" ]; then
    echo "DEBUG: debug only, no push ..."
else
    git push origin $RELEASE_BRANCH
    git push origin $RELEASE_VERSION
fi

#  cd $WORKROOT
#  echo "INFO: Updating www docs ..."
#  if [ -d "www" ]; then
#      rm -rf www
#  fi
#  echo "INFO: svn checkout --non-interactive --depth=empty https://svn.java.net/svn/jaxb~www/trunk/www"
#  svn checkout --non-interactive --depth=empty https://svn.java.net/svn/jaxb~www/trunk/www
#  # create www release folder and copy the built out docs
#  cd www || exit 1
#  mkdir -p $RELEASE_VERSION
#  echo "INFO: cp $WORKROOT/jaxb-ri/jaxb-ri/docs/www/target/index.html $RELEASE_VERSION/"
#  cp $WORKROOT/jaxb-ri/jaxb-ri/docs/www/target/index.html $RELEASE_VERSION/
#  mkdir -p $RELEASE_VERSION/docs
#  echo "INFO: cp -r $WORKROOT/jaxb-ri/jaxb-ri/docs/release-documentation/target/docbook/* $RELEASE_VERSION/docs"
#  cp -r $WORKROOT/jaxb-ri/jaxb-ri/docs/release-documentation/target/docbook/* $RELEASE_VERSION/docs
#  echo "INFO: cp ${WORKROOT}/jaxb-ri/jaxb-ri/bundles/ri/target/jaxb-ri.zip ${RELEASE_VERSION}/jaxb-ri-${RELEASE_VERSION}.zip"
#  cp ${WORKROOT}/jaxb-ri/jaxb-ri/bundles/ri/target/jaxb-ri.zip ${RELEASE_VERSION}/jaxb-ri-${RELEASE_VERSION}.zip
#  cd $WORKROOT/jaxb-ri/jaxb-ri
#  # clean the jaxb build workspace and zip the src
#  echo "INFO: Generating the source zip ..."
#  mvn $MAVEN_SETTINGS  $MAVEN_LOCAL_REPO clean
#  zip -r -q ../jaxb-ri-${RELEASE_VERSION}.src.zip *
#  cd ..
#  cp jaxb-ri-${RELEASE_VERSION}.src.zip jaxb-ri.licensee.zip
#  svn export https://svn.java.net/svn/jaxb~www/trunk/www/release-scripts/TLDA_SCSL_Licensees_License_Notice
#  cp ./TLDA_SCSL_Licensees_License_Notice ./TLDA_SCSL_Licensees_License_Notice.txt
#  zip -u -q jaxb-ri.licensee.zip TLDA_SCSL_Licensees_License_Notice.txt || exit 1
#
#  cd ${WORKROOT}/www
#  echo "INFO: cp ${WORKROOT}/jaxb-ri/jaxb-ri-${RELEASE_VERSION}.src.zip ${RELEASE_VERSION}/"
#  cp ${WORKROOT}/jaxb-ri/jaxb-ri-${RELEASE_VERSION}.src.zip ${RELEASE_VERSION}/
#  echo "INFO: cp ${WORKROOT}/jaxb-ri/jaxb-ri.licensee.zip ${RELEASE_VERSION}/"
#  cp ${WORKROOT}/jaxb-ri/jaxb-ri.licensee.zip ${RELEASE_VERSION}/
#  svn add --non-interactive $RELEASE_VERSION
#
#  # link the latest relase to current release
#  echo "INFO: Update latest download page link to $RELEASE_VERSION"
#  svn --non-interactive update -q latest
#  sed -i "s#URL=https://jaxb.java.net/.*/#URL=https://jaxb.java.net/$RELEASE_VERSION/#" latest/download.html
#  sed -i "s#URL=https://jaxb.java.net/.*/#URL=https://jaxb.java.net/$RELEASE_VERSION/docs/#" latest/docs.html
#
#  # modify www/downloads/ri/index.html
#  svn --non-interactive update -q downloads
#  RELEASE_VERSION_MAIN=${RELEASE_VERSION%.*}
#  set +e
#  grep -q "<h2>JAXB RI $RELEASE_VERSION_MAIN<\/h2>" downloads/ri/index.html
#  if [ $? -eq 0 ]; then
#      echo "INFO: found $RELEASE_VERSION_MAIN snippet, updating downloads/ri/index.html to current release..."
#      sed -i -e "s#Download the <a href=\"../../$RELEASE_VERSION_MAIN.*/jaxb-ri-$RELEASE_VERSION_MAIN.*.zip\">JAXB $RELEASE_VERSION_MAIN.* binary distribution</a><br/>#Download the <a href="../../$RELEASE_VERSION/jaxb-ri-$RELEASE_VERSION.zip">JAXB $RELEASE_VERSION binary distribution</a><br/>#" -e "s#Download the <a href=\"../../$RELEASE_VERSION_MAIN.*/jaxb-ri-$RELEASE_VERSION_MAIN.*.src.zip\">JAXB $RELEASE_VERSION_MAIN.* source distribution</a><br/>#Download the <a href="../../$RELEASE_VERSION/jaxb-ri-$RELEASE_VERSION.src.zip">JAXB $RELEASE_VERSION source distribution</a><br/>#" downloads/ri/index.html
#  else
#      echo "INFO: adding new $RELEASE_VERSION_MAIN snippet into downloads/ri/index.html"
#      tmpfile=$TMPDIR/release_jaxb_newentry_$$
#      rm -f $tmpfile
#      cat > $tmpfile <<EOF
#  <h2>JAXB RI $RELEASE_VERSION_MAIN</h2>
#
#  Download the <a href="../../$RELEASE_VERSION/jaxb-ri-$RELEASE_VERSION.zip">JAXB $RELEASE_VERSION binary distribution</a><br/>
#  Download the <a href="../../$RELEASE_VERSION/jaxb-ri-$RELEASE_VERSION.src.zip">JAXB $RELEASE_VERSION source distribution</a><br/>
#
#  EOF
#      # I maybe figure out a better way of not hardcode the line number(10) here later
#      sed  -i "10 r $tmpfile" downloads/ri/index.html
#      rm -f $tmpfile
#  fi

# modify www/__modules/left_sidebar.htmlx
# echo "INFO: add $RELEASE_VERSION to the left side bar"
# svn --non-interactive update -q __modules
# line=`sed -n '/       <li> <a href=\"#\">Download<\/a>/=' __modules/left_sidebar.htmlx`
# line=`expr $line + 1`
# appendLine="\ \ \ \ \ \ \ \ \ \ \ \ \ \ \ \ <li><a href=\"http://jaxb.java.net/$RELEASE_VERSION/\">$RELEASE_VERSION</a><\/li>"
# sed -i "$line a\
# $appendLine" __modules/left_sidebar.htmlx
# sed -i -e "s#<li> <a href=\"http://jaxb.java.net/nonav/.*/docs/\">Latest release notes</a>#<li> <a href=\"http://jaxb.java.net/nonav/$RELEASE_VERSION/docs/\">Latest release notes</a>#" -e "s#<li> <a href=\"http://jaxb.java.net/nonav/.*/docs/api/\">Javadoc</a></li>#<li> <a href=\"http://jaxb.java.net/nonav/$RELEASE_VERSION/docs/api/\">Javadoc</a></li>#" __modules/left_sidebar.htmlx
#
# if [ -n "$WWW_SVN_USER"  -a -n "$WWW_SVN_PASSWORD" ]; then
#     AUTH="--username $WWW_SVN_USER --password $WWW_SVN_PASSWORD --no-auth-cache"
# else
#     AUTH=""
# fi
# if [ "$debug" = "true" ]; then
#     echo "DEBUG: debug only, not commit the docs."
#     echo "DEBUG: svn $AUTH --non-interactive --no-auth-cache commit -m \"JAXB release $RELEASE_VERSION\""
# else
#     echo "INFO: commit the updated docs"
#     svn $AUTH --non-interactive --no-auth-cache commit -m "JAXB release $RELEASE_VERSION" . || exit 1
# fi
