if [[ "$1" == 201* ]]; then
  VERSION=$1.RELEASE
else
  SUFFIX=$1
  VERSION=`date +%Y.%m.%d$SUFFIX.RELEASE`
fi

EXISTS=`curl -s --head https://oss.sonatype.org/service/local/repositories/releases/content/com/revolsys/open/com.revolsys.open.parent/$VERSION/ |head -1 |grep 200`
if [ -z "$EXISTS" ]; then
  STATUS=`git status | grep "nothing to commit"`
  if [ -n "$STATUS" ]; then
    mvn clean
  
    git pull
    git branch releases
    git checkout releases
    git merge master
  
    find . -name pom.xml  -exec sed -i "" -e "s/TRUNK-SNAPSHOT/$VERSION/g" {} \;
    git commit -a -m "Release $VERSION"
    git tag -f $VERSION
    
    git checkout -f master
    git branch -D releases
    git push origin
    git push origin :$VERSION
    git push origin $VERSION
  
    mvn release:perform -DconnectionUrl=scm:git:git@github.com:revolsys/com.revolsys.open.git -Dtag=$VERSION -DlocalCheckout=true
    if [ $? -eq 0 ]; then
      mvn nexus:staging-close -Dnexus.version=$VERSION -DserverAuthId=sonatype-nexus-staging -Dnexus.automaticDiscovery=true
      if [ $? -eq 0 ]; then
        mvn nexus:staging-release -Ddescription="Staging Releasing $VERSION" -DtargetRepositoryId=releases -Dnexus.version=$VERSION -Dnexus.promote.autoSelectOverride=true -DserverAuthId=sonatype-nexus-staging -Dnexus.automaticDiscovery=true
      fi
    fi
  else
    echo Checkin changes before releasing
  fi
else
  echo Version $VERSION exists
fi