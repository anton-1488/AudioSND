git add .
git commit -m "Update brances"

git checkout $1
git merge main
git checkout main

clear

git branch