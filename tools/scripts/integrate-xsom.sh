#!/bin/sh
# shell script to integrate xsom from Hudson
cd $JAXB_HOME
wget -O tools/lib/rebundle/compiler/xsom.jar \
	http://kohsuke.sfbay/hudson/job/xsom/lastSuccessfulBuild/artifact/xsom/build/xsom.jar
wget -O tools/lib/src/xsom-src.zip \
	http://kohsuke.sfbay/hudson/job/xsom/lastSuccessfulBuild/artifact/xsom/build/xsom-src.zip
