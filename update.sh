git add .
git commit -m "Pre update teask"

git checkout $1
git merge main
git checkout main

clear

git branch