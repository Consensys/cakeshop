# Configuration

## Cakeshop

Cakeshop follows standard [Spring Boot configuration patterns](https://docs.spring.io/spring-boot/docs/2.3.7.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config), so you may provide an `application.properties` in the location where you are running Cakeshop to customize some of its settings.

You may also override config options via Java system properties passed via `java -D<prop>=<val>`:

```sh
java -Dcakeshop.initialnodes=data/cakeshop/nodes.json -jar cakeshop.war
```

### Initial Nodes

When Cakeshop starts for the first time, it will not actually know which node it is supposed to connect to. You will need to click on 'Manage Nodes' in the top right corner of the Cakeshop UI to add nodes by RPC URL.

Alternatively, you may provide an initial set of nodes in a JSON file that Cakeshop will use to prepopulate the nodes list. This file will only be used if no nodes have previously been added to Cakeshops database.

The format of the JSON file is as follows:

```json
[
  {
    "name": "node1",
    "rpcUrl": "http://localhost:22000",
    "transactionManagerUrl": "http://localhost:9081"
  },
  {
    "name": "node2",
    "rpcUrl": "http://127.0.0.1:22001",
    "transactionManagerUrl": "http://127.0.0.1:9082"
  },
  {
    "name": "node3",
    "rpcUrl": "http://localhost:22002",
    "transactionManagerUrl": "http://localhost:9083"
  }
]
```

The `rpcUrl` field should be the RPC endpoint on the GoQuorum (`geth`) node, and the `transactionManagerUrl` should be the Tessera 3rd party API endpoint.

Provide the location of the initial nodes file through `application.properties` or by setting the `cakeshop.initialnodes` property with Java `-D` command line option.

```properties
# inside application.properties
cakeshop.initialnodes=path/to/nodes.json
```

### Database

Cakeshop uses Spring Data for its database connection. By default, it uses an in-memory/file-based HSQLDB, but you may customize using standard Spring Data config values:

```properties
# spring data settings
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.url=jdbc:postgresql://localhost:5432/cakeshop
spring.jpa.hibernate.ddl-auto=update
spring.datasource.hikari.auto-commit=false
spring.datasource.username=sdk
spring.datasource.password=sdk
```

### Cakeshop Internals

Some other options that may be customized in `application.properties`:

```properties
# some systems don't call the nodejs binary 'node', in that change you can change this value
nodejs.binary=node

# if you are migrating from an older version of cakeshop, you can migrate registered contracts
# from the old smart contract storage to the cakeshop database
contract.registry.addr=0xCONTRACT_ADDRESS_HERE

# if you are using the quorum reporting engine, you may tell cakeshop the location of its rpc and ui endpoints.
cakeshop.reporting.rpc=http://localhost:4000
cakeshop.reporting.ui=http://localhost:3000

# port to run on
server.port=8080

#logging levels
logging.level.root=WARN
logging.level.org.springframework=INFO
logging.level.com.jpmorgan.cakeshop=INFO
```

See the [default config file](../cakeshop-api/src/main/resources/config/application.properties) for more. 
