#!/bin/sh
# This script is for setting up git hooks

#Copies the hooks to the .git directory

if [ ! -d "hooks" ]; then 
git remote add hooks https://github.com/ekstep/GitHooks.git
git subtree add --prefix=hooks/ --squash hooks  master
else
git subtree pull --prefix=hooks/ --squash hooks  master
fi
cp hooks/prepare-commit-msg .git/hooks/prepare-commit-msg

exit 1;
