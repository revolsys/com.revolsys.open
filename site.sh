STATUS=`git status | grep "nothing to commit"`
if [ -n "$STATUS" ]; then
  git push
  git pull
  mvn clean site

  git clone .git -b gh-pages target/gh-pages
  
  rm -rf target/gh-pages/*
  for dir in `find . -type d -name site`; do
    module=${dir//.\//}
    module=${module//target\/site/}
    module=${module//\//}
    
    rsync -a ${dir}/ target/gh-pages/${module}/
  done
  cd target/gh-pages
  git commit -a -m "Site update"
  git push
  cd ../..
  git push origin gh-pages
else
  echo Checkin changes before creating site
fi
