- To start node-manager in Spring Boot mode  just execute this command java -jar cakeshop-node-manager.war
- To start it inside tomcat create setenv.sh(bat) file in tomcat/bin add following line to it -Dspring.profiles.active=container
- By default node-manager runs on embedded database (easy for development). It can also run on external database and we  recommend to run it on external database when in production or staging mode.
  The example how to configure external database for spring-boot is in application.properties 
  To run within tomcat using data source have this property added into properties file nodemanager.jndi.name=<your_datasorce_name> and nodemanager.database.vendor=oracle|postgres|mysql
  or you can add -Dnodemanager.jndi.name=<your_datasorce_name> and -Dnodemanager.database.vendor=oracle|postgres|mysql into setenv.sh(bat) file
