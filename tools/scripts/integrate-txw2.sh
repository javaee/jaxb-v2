#!/bin/sh
# shell script to integrate txw2 from Hudson
cd $JAXB_HOME
wget -O tools/lib/rebundle/compiler/txw2.jar \
	http://kohsuke.sfbay/hudson/job/txw2/lastSuccessfulBuild/artifact/txw2/build/txw2.jar
wget -O tools/lib/util/txwc2.jar \
	http://kohsuke.sfbay/hudson/job/txw2/lastSuccessfulBuild/artifact/txw2/build/txwc2.jar
wget -O tools/lib/src/txw2-src.zip \
	http://kohsuke.sfbay/hudson/job/txw2/lastSuccessfulBuild/artifact/txw2/build/txw2-src.zip
