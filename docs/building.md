# Building

## Building a Docker Image

```
# build custom maven image
docker build -t cakeshop-build docker/build/

# build cakeshop.war
docker run --rm -v ~/.m2:/home/cakeshop/.m2 -v $(pwd):/usr/src -w /usr/src cakeshop-build mvn -DskipTests clean package

# build cakeshop image using war from previous step
mv cakeshop-api/target/cakeshop*.war docker/cakeshop/
docker build -t cakeshop docker/cakeshop/
```
