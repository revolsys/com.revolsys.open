#!/bin/bash

if [[ "$1" == 201* ]]; then
  VERSION=$1.RELEASE
else
  VERSION=`date +%Y.%m.%d.RELEASE`
fi

git pull
git branch releases
git checkout releases
git merge master
git tag -d $VERSION
git push origin :$VERSION
mvn release:clean
mvn release:prepare -DautoVersionSubmodules=true -DdevelopmentVersion=TRUNK-SNAPSHOT -DreleaseVersion=${VERSION} -Dtag=${VERSION} -DupdateWorkingCopyVersions=false -DpreparationGoals=clean
mvn release:perform
git checkout master
git branch -D releases
git push origin
git push origin :$VERSION
git push origin $VERSION