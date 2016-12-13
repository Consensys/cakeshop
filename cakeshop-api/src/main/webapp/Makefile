.PHONY: all webpack move

SERVE_DIR=/usr/local/Cellar/tomcat/8.5.4/libexec/webapps/cakeshop

all: webpack move

webpack:
	./node_modules/.bin/webpack --config webpack.config.js

move:
	cp -r * $(SERVE_DIR)
