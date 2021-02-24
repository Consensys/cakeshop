# Cakeshop from scratch

This worked as described on a VM image of Ubuntu 16.04. Note that Haskell is memory-hungry and needs 2GB at a minimum, possibly more just to complete the `install` process. It also works with `brew` on macOS with somewhat less effort. These steps work as of 2018, Jul 1.

## Base environment

Some of these may already exist on the system you're using. Go lang needs to be 1.7+, Java 7+, there is some minimum requirement for Haskell as well, so just take latest. Minimum requirements for Berkeley DB (libdb) are also there. Extra steps around go-lang are only needed for the default Ubuntu 16.04 image. 

```shell
sudo apt-get install openjdk-8-jdk
sudo apt-get install git
sudo apt-get install maven
sudo apt-get install nodejs
sudo apt-get install npm
sudo apt-get install golang-1.10-go
sudo rm /usr/bin/go
sudo ln /usr/lib/go-1.10/bin/go /usr/bin/go
sudo apt-get install curl
curl -sSL https://get.haskellstack.org/ | sh
sudo ln /usr/local/bin/stack /usr/bin/stack
sudo apt-get install libsodium-dev
sudo apt-get install libleveldb-dev
sudo apt-get install libdb5.3-dev
sudo apt-get install zlib1g-dev
sudo apt-get install libtinfo-dev
```

## Cakeshop build

```shell
mkdir ~/Projects/cakeshop.git
cd ~/Projects/cakeshop.git
git clone https://github.com/ConsenSys/cakeshop.git
```

```shell
cd ~/Projects/cakeshop.git/cakeshop
mvn clean install -DskipTests
cd cakeshop-api
mvn install -DskipTests
java -jar target/cakeshop-0.12.0.war
```

## Related projects

```shell
mkdir ~/Projects/istanbul-tools.git
cd ~/Projects/istanbul-tools.git
git clone -b master --single-branch https://github.com/getamis/istanbul-tools.git
```

```shell
mkdir ~/Projects/constellation.git
cd ~/Projects/constellation.git
git clone -b master --single-branch https://github.com/ConsenSys/constellation.git
```

```shell
mkdir ~/Projects/quorum.git
cd ~/Projects/quorum.git
git clone -b master --single-branch https://github.com/ConsenSys/quorum.git
```

## Related builds

These steps aren't strictly necessary. The repo was updated with the below binaries as listed on 2018, Jun 30.

```shell
cd ~/Projects/quorum.git/quorum
make all
mv ./build/bin/geth ~/Projects/cakeshop.git/cakeshop/cakeshop-api/src/main/resources/geth/quorum/linux/geth
mv ./build/bin/bootnode ~/Projects/cakeshop.git/cakeshop/cakeshop-api/src/main/resources/geth/quorum/linux/bootnode
```

```shell
cd ~/Projects/constellation.git/constellation
stack setup
stack install
mv ~/.local/bin/constellation-node ~/Projects/cakeshop.git/cakeshop/cakeshop-api/src/main/resources/geth/quorum/constellation/linux/constellation-node
```

Note that the istanbul build will report a failure with `urfave/cli`, ignore it and continue.

```shell
cd ~/Projects/istanbul-tools.git/istanbul-tools
go get github.com/getamis/istanbul-tools/cmd/istanbul
make
mkdir ~/Projects/cakeshop.git/cakeshop/cakeshop-api/src/main/resources/geth/quorum/istanbul-tools/linux
mv ~/go/bin/istanbul ~/Projects/cakeshop.git/cakeshop/cakeshop-api/src/main/resources/geth/quorum/istanbul-tools/linux/istanbul
```
