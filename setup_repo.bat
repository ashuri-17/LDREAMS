@echo off
cd /d "%~dp0"
git init
git checkout -b main
git add -A
git commit -m "Initial commit: LDREAMS lucid dreaming Android app"
echo Repository initialized successfully
