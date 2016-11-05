#!/bin/bash
pushd ../../maki
git pull
popd

rm -f src/main/resources/maki/*.svg
for file in ../../maki/src/*-18.svg; do
  filename=`basename $file`
  iconName=${filename%-18.svg}
  cp $file src/main/resources/maki/$iconName.svg
done
