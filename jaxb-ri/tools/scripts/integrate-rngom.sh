#!/bin/sh
# shell script to integrate xsom from Hudson
cd $JAXB_HOME
wget -O tools/lib/rebundle/compiler/rngom.jar \
	http://kohsuke.sfbay/hudson/job/rngom/lastSuccessfulBuild/artifact/rngom/rngom/build/rngom.jar
wget -O tools/lib/src/rngom-src.zip \
	http://kohsuke.sfbay/hudson/job/rngom/lastSuccessfulBuild/artifact/rngom/rngom/build/rngom-src.zip
