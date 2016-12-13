# Configuration

## Tomcat

Minimum requirements for tomcat (`conf/server.xml`):

```xml
  <Connector port="8080" protocol="HTTP/1.1"
             enableLookups="false"
             maxKeepAliveRequests="-1"
             maxConnections="10000"
             redirectPort="8443"
             connectionTimeout="20000"/>
```

## Database

Currently Cakeshop supports Oracle, Postgres, MySQL, and HSQL databases.

The following system properties are available (passed via `java -D<prop>=<val>`):

```
cakeshop.database.vendor              Enables the preferred db driver.
                                      Allowed options are:
                                      hsqldb|oracle|mysql|postgres
                                      Default: hsqldb

cakeshop.jndi.name                    Used for configuring an external
                                      connection pool (usually container-
                                      managed) with oracle, mysql or postgres.

cakeshop.jdbc.url                     JDBC URL
cakeshop.jdbc.user                    JDBC username
cakeshop.jdbc.pass                    JDBC password

cakeshop.hibernate.jdbc.batch_size    Hibernate tuneables
cakeshop.hibernate.hbm2ddl.auto

cakeshop.hibernate.dialect            Hibernate dialect. Will usually be set
                                      automatically by your db pref. Override
                                      it here.
```
