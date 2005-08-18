#!/bin/bash -x
#
# deploy javadoc to the java.net web site. This is used by Hudson.
# 
cd build

if [ -e www ]
then
  cd www
  cvs update -d
  cd ..
else
  cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z9 co -d www jaxb2-reflection/www
fi

cd www

cp -R ../javadoc/* javadoc

# ignore everything under CVS, then
# ignore all files that are already in CVS, then
# add the rest of the files
find . -name CVS -prune -o -exec bash ../../../tools/scripts/in-cvs.sh {} \; -o \( -print -a -exec cvs add {} \+ \)

# sometimes the first commit fails
cvs commit -m "commit 1 " || cvs commit -m "commit 2"
