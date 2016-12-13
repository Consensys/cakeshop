FROM maven:3-jdk-8

RUN set -x \
    && addgroup cakeshop \
    && useradd -g cakeshop -s /bin/bash --home-dir /home/cakeshop -m cakeshop

USER cakeshop

RUN mkdir -p $HOME/.m2

ENV USER_HOME_DIR "/home/cakeshop"
ENV MAVEN_CONFIG "/home/cakeshop/.m2"
