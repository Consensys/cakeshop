
# UPDATE WIN BINARY

# OPEN GIT BASH

# BUILD
export GOPATH="/c/godev"
export PATH="$PATH:$GOROOT\bin:$GOPATH\bin:c:\winbuilds\bin"
cd /c/godev/src/github.com/ethereum/go-ethereum/cmd/geth
git pull
godep go install

# COMMIT
# new binary is now in /c/godev/bin/geth.exe
cp -a /c/godev/bin/geth.exe /c/Users/chetan/workspace/cakeshop/cakeshop-api/src/main/resources/geth/bin/win/
cd /c/Users/chetan/workspace/cakeshop
git pull
git add -u /c/Users/chetan/workspace/cakeshop/cakeshop-api/src/main/resources/geth/bin/win/geth.exe
git commit
