# Build
FROM maven:3.6.3-jdk-11 as builder

RUN mkdir -p $HOME/.m2

ADD . /home/cakeshop

RUN cd /home/cakeshop && mvn clean -DskipTests package

# Create docker image with only distribution jar
FROM adoptopenjdk/openjdk11:alpine

RUN apk add nodejs

# Cakeshop data directory is a volume, so it can be persisted and
# survive image upgrades
VOLUME /cakeshop/data

WORKDIR /cakeshop

COPY --from=builder /home/cakeshop/cakeshop-api/target/cakeshop*.war /cakeshop/cakeshop.war

# for main web interface
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/cakeshop/cakeshop.war"]
