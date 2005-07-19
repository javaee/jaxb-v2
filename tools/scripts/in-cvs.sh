#!/bin/bash -x
# checks if the argument is under the control of CVS.
# if it's in, return 0, otherwise 1
ent="`dirname "$1"`/CVS/Entries"
if [ ! -e "$ent" ];
then
  # no entry file
  exit 1
fi

grep "\/`basename "$1"`\/" "$ent" > /dev/null
if [ $? -eq 0 ];
then
  # found
  exit 0
else
  # not
  exit 1
fi