STATUS=`git status | grep "nothing to commit"`
if [ -n "$STATUS" ]; then
  mvn clean

  git pull
  git checkout gh-pages
  
  for dir in `find . -type d -name site`; do
    module=${dir/.\//}
    module=${dir/\/target\/site\//}
    echo $module
  done

  git checkout master
else
  echo Checkin changes before creating site
fi
