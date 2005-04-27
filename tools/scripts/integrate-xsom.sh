#!/bin/sh
# shell script to integrate xsom from Hudson
if [ "$JAXB_HOME" != "" ]; then
  cd $JAXB_HOME
fi
wget -O tools/lib/rebundle/compiler/xsom.jar \
	http://kohsuke.sfbay/hudson/job/xsom/lastSuccessfulBuild/artifact/jaxb2-sources/xsom/build/xsom.jar
wget -O tools/lib/src/xsom-src.zip \
	http://kohsuke.sfbay/hudson/job/xsom/lastSuccessfulBuild/artifact/jaxb2-sources/xsom/build/xsom-src.zip
