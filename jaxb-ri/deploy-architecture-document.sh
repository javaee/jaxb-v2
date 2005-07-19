#!/bin/bash -x
#
# generate the architecture document and deploy that into the java.net CVS repository
# 

ant architecture-document

cd build

if [ -e jaxb-architecture-document-www ]
then
  cvs update -Pd
else
  cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z9 co -d jaxb-architecture-document-www jaxb-architecture-document/www
fi

cd jaxb-architecture-document-www

cp -R ../javadoc/* doc

# swallow the error code
find . -name CVS -prune -or -exec cvs add {} \+ -or -true

date >> update.html

cvs commit -m "to work around a bug in java.net web updater"
