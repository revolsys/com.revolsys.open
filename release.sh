if [[ "$1" == 201* ]]; then
  VERSION=$1.RELEASE
else
  VERSION=`date +%Y.%m.%d.RELEASE`
fi

mvn clean

git pull
git branch releases
git checkout releases
git merge master

find . -name pom.xml  -exec sed -i "s/TRUNK-SNAPSHOT/$VERSION/g" {} \;

git tag -f $VERSION
git checkout master
git branch -D releases
git push origin
git push origin :$VERSION
git push origin $VERSION

mvn release:perform -Dmaven.test.skip=true -tag $VERSION
