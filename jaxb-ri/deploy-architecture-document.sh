#!/bin/sh -x
#
# generate the architecture document and deploy that into the java.net CVS repository
# 
ant architecture-document
cd build/javadoc
cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z3 import -ko -W "*.png -k 'b'" -W "*.gif -k 'b'" -m "deploying the new web contents" jaxb-architecture-document/www/doc site-deployment t`date +%Y%m%d-%H%M%S`
cd ..
cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z3 co -l -d jaxb-architecture-document-www jaxb-architecture-document/www
cd jaxb-architecture-document-www
date >> update.html
cvs commit -m "to work around a bug in java.net web updater" update.html
