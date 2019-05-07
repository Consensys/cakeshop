
# Folders

    bin             platform-specific geth & node binaries for mac, linux and windows (only amd64 supported)
    genesis         files for initializing a new ethereum setup (keystore, genesis block, password file)
    solc            solc-cli node.js app

# Updating geth binaries

Make sure GOPATH and CAKEPATH are set
`export CAKEPATH=~/work/cakeshop`

## Building for all platforms (via cross-compile with xgo)
```
cd $GOPATH/src/github.com/ethereum/go-ethereum
go get -ldflags "-X main.gitCommit=$(git rev-parse HEAD)" github.com/ethereum/go-ethereum/cmd/geth
build/bin/xgo --go=latest --dest=build/bin/ --targets=windows/amd64 --ldflags "-X main.gitCommit=$(git rev-parse HEAD)" ./cmd/geth
build/bin/xgo --go=latest --dest=build/bin/ --targets=linux/amd64 --ldflags "-X main.gitCommit=$(git rev-parse HEAD)" ./cmd/geth

# cross-compile command for mac (not needed)
#build/bin/xgo --go=latest --dest=build/bin/ --targets=darwin/amd64 --ldflags "-X main.gitCommit=$(git rev-parse HEAD)" ./cmd/geth
```

## Copy to cakeshop & commit
```
cp -a `which geth` $CAKEPATH/cakeshop-api/src/main/resources/geth/bin/mac/geth
cp -a build/bin/geth-linux-amd64 $CAKEPATH/cakeshop-api/src/main/resources/geth/bin/linux/geth
cp -a build/bin/geth-windows-4.0-amd64.exe $CAKEPATH/cakeshop-api/src/main/resources/geth/bin/win/geth.exe
```


## On OS X & Linux
```
cd $GOPATH/src/github.com/ethereum/go-ethereum && git pull
go get -ldflags "-X main.gitCommit=$(git rev-parse HEAD)" github.com/ethereum/go-ethereum/cmd/geth

cd $CAKEPATH && git pull

# macOS
cp -a $GOPATH/bin/geth $CAKEPATH/cakeshop-api/src/main/resources/geth/bin/mac/geth

# linux
cp -a $GOPATH/bin/geth $CAKEPATH/cakeshop-api/src/main/resources/geth/bin/linux/geth

git commit -m "[geth] updated mac/linux to 1.4.6-stable-5422cbbd-gemini ..."
git push
```

## On Windows
```
# build
export CAKEPATH=/c/Users/chetan/workspace/cakeshop
export GOPATH="/c/godev"
export PATH="$PATH:$GOROOT\bin:$GOPATH\bin:c:\winbuilds\bin"
cd $GOPATH/src/github.com/ethereum/go-ethereum
git pull
go get -ldflags "-X main.gitCommit=$(git rev-parse HEAD)" github.com/ethereum/go-ethereum/cmd/geth

# commit
cd $CAKEPATH && git pull
cp -a $GOPATH/bin/geth.exe cakeshop-api/src/main/resources/geth/bin/win/
git add -u cakeshop-api/src/main/resources/geth/bin/win/geth.exe
git commit -m "[geth] updated windows to 1.4.6-stable-5422cbbd-gemini ..."
```
