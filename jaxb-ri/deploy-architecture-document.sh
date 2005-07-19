#!/bin/bash -x
#
# generate the architecture document and deploy that into the java.net CVS repository
# 

ant architecture-document

cd build

if [ -e jaxb-architecture-document-www ]
then
  cd jaxb-architecture-document-www
  cvs update -Pd
  cd ..
else
  cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z9 co -d jaxb-architecture-document-www jaxb-architecture-document/www
fi

cd jaxb-architecture-document-wwwh

cp -R ../javadoc/* doc

# ignore everything under CVS, then
# ignore all files that are already in CVS, then
# add the rest of the files
find . -name CVS -prune -o -exec sh tools/in-cvs.{} \; -o -exec cvs add {} \+

date >> update.html

cvs commit -m "to work around a bug in java.net web updater"
