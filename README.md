# Documentation updates v0.9.x -> v0.10.x are in progress. Sections marked NEEDS UPDATE may not behave as expected.


# Cakeshop

An integrated development environment and SDK for Ethereum-like ledgers

![screenshot](docs/images/screenshot.png "screenshot")

![screenshot](docs/images/sandbox.png "sandbox screenshot")

## What is it?

_Cakeshop_ is a set of tools and APIs for working with [Ethereum](https://ethereum.org/)-like ledgers, packaged as a Java web application archive (WAR) that gets you up and running in under 60 seconds.

Included in the package is the [geth](https://github.com/ethereum/go-ethereum), [quorum](https://github.com/jpmorganchase/quorum), and [constellation](https://github.com/jpmorganchase/constellation) Ethereum servers, a [Solidity](https://solidity.readthedocs.org/en/latest/)
compiler and all dependencies.

It provides tools for managing a local blockchain node, setting up clusters,
exploring the state of the chain, and working with contracts.

## Download

Binary packages are available for macOS, Windows, and Linux platforms on the [releases](https://github.com/jpmorganchase/cakeshop/releases) page.

## Quickstart

### Requirements

* Java 7+
* Java app server (Tomcat, Jetty, etc) [Optional]

### Running via Spring Boot

* Download WAR file
* Run `java -jar cakeshop.war`
* Navigate to [http://localhost:8080/cakeshop/](http://localhost:8080/cakeshop/)

*Note: when running in Windows, -Dgeth.node=geth must be specified as Quorum is not yet available on Windows OS*

### Running via App Server

* Download WAR file
* Put in `/webapps` folder of your app server
* Add Java system property `-Dspring.profiles.active=local` to startup script (`setenv.sh` for tomcat)
* Start app server
* Navigate to [http://localhost:8080/cakeshop/](http://localhost:8080/cakeshop/) (default port is usually 8080)

*Note: when running in Windows, -Dgeth.node=geth must be specified as Quorum is not yet available on Windows OS*

### Running via Docker -- NEEDS UPDATE

Run via docker and access UI on [http://localhost:8080/cakeshop/](http://localhost:8080/cakeshop/)

```sh
docker run -p 8080:8080 jpmc/cakeshop
```

You'll probably want to mount a data volume:

```sh
mkdir data
docker run -p 8080:8080 -v "$PWD/data":/opt/cakeshop/data jpmc/cakeshop
```

Running under a specific environment

```sh
docker run -p 8080:8080 -v "$PWD/data":/opt/cakeshop/data \
    -e JAVA_OPTS="-Dspring.profiles.active=local" \
    jpmc/cakeshop
```

Note that DAG generation will take time and Cakeshop will not be available until it's complete. If you already have a DAG for epoch 0 in your `$HOME/.ethash` folder, then you can expose that to your container (or just cache it for later):

```sh
docker run -p 8080:8080 -v "$PWD/data":/opt/cakeshop/data \
    -v $HOME/.ethash:/opt/cakeshop/.ethash \
    jpmc/cakeshop
```

## Further Reading

Further documentation can be found on the [wiki](https://github.com/jpmorganchase/cakeshop/wiki/) and in the [docs](docs/) folder.

## See Also

* [JIF Dashboard](https://github.com/jpmorganchase/jif-dashboard) - The Cakeshop UI was built using the JIF Dashboard framework.

* [solc-cakeshop-cli](https://github.com/jpmorganchase/solc-cakeshop-cli) - The solidity compiler used behind the scenes is `solc-cakeshop-cli`, a thin wrapper atop the [solc](https://github.com/ethereum/solc-js) JS binding.

## Contributing

Thank you for your interest in contributing to Cakeshop!

### How to generate war file
	mvn clean install 

### Where is the generated war file
	cakeshop/cakeshop-api/target

### Troubleshoot
	Sometimes you need sudo permissions to allow bower to install packages. You have to make few changes in pom.xml. I have done those changes in dev branch so you can check that out.

Cakeshop is built on open source and we invite you to contribute enhancements. Upon review you will be required to complete a Contributor License Agreement (CLA) before we are able to merge. If you have any questions about the contribution process, please feel free to send an email to [quorum_info@jpmorgan.com](mailto:quorum_info@jpmorgan.com).

## License

Copyright (c) 2016-2017 JPMorgan Chase and/or applicable contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

See the [LICENSE](LICENSE) and [THIRD_PARTY](THIRD_PARTY) files for additional license information.
