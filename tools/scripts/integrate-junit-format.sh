#!/bin/sh
# shell script to integrate JUnit format from Hudson
cd $JAXB_HOME
wget -O tools/lib/util/junit-format.jar \
	http://kohsuke.sfbay/hudson/job/junit-format/lastSuccessfulBuild/artifact/junit-format/build/junit-format.jar
wget -O tools/lib/src/junit-format.src.zip \
	http://kohsuke.sfbay/hudson/job/junit-format/lastSuccessfulBuild/artifact/junit-format/build/junit-format.src.zip
