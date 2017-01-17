@echo off

echo Copy hooks to .git repo
IF EXIST "hooks\" (
call git subtree pull --prefix=hooks\ --squash hooks  master
) ELSE (
call git remote add hooks https://github.com/ekstep/GitHooks.git
call git subtree add --prefix=hooks\ --squash hooks  master
)
xcopy "hooks\prepare-commit-msg" ".git\hooks\" /y
