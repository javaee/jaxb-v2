#!/bin/sh
# shell script to integrate jsr222-mirror from Hudson
cd $JAXB_HOME
wget -O tools/lib/redist/jaxb-api.jar \
	http://kohsuke.sfbay/hudson/job/jsr222-mirror/lastSuccessfulBuild/artifact/jsr222-mirror/dist/jaxb-api.jar
wget -O tools/lib/redist/jaxb-api-src.zip \
	http://kohsuke.sfbay/hudson/job/jsr222-mirror/lastSuccessfulBuild/artifact/jsr222-mirror/dist/jaxb-api-src.zip
wget -O tools/lib/redist/jaxb-api-doc.zip \
	http://kohsuke.sfbay/hudson/job/jsr222-mirror/lastSuccessfulBuild/artifact/jsr222-mirror/dist/jaxb-api-doc.zip
