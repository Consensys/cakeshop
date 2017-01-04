FROM openjdk:8-jdk

# Cakeshop is run with user `cakeshop`, uid = 1000
ARG user=cakeshop
ARG group=cakeshop
ARG uid=1000
ARG gid=1000

ENV CAKESHOP_USER=$user
ENV CAKESHOP_GROUP=$group
ENV CAKESHOP_HOME /opt/cakeshop

# tini as PID 1
# gosu to drop privs
ENV TINI_VERSION 0.11.0
ENV TINI_SHA 7c18e2d8fb33643505f50297afddc8bcac5751c8a219932143405eaa4cfa2b78
ENV GOSU_VERSION 1.10

RUN set -x \
    && addgroup --gid ${gid} --system ${group} \
    && adduser --system --home "$CAKESHOP_HOME" --shell /sbin/nologin --ingroup ${group} ${user} \
    && apt-get update \
    && apt-get -y install curl \
    && curl -fsSL https://github.com/krallin/tini/releases/download/v${TINI_VERSION}/tini-static -o /bin/tini && chmod +x /bin/tini \
    && echo "$TINI_SHA  /bin/tini" | sha256sum -c - \
    && apt-get install -y --no-install-recommends ca-certificates wget && rm -rf /var/lib/apt/lists/* \
    && dpkgArch="$(dpkg --print-architecture | awk -F- '{ print $NF }')" \
    && wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch" \
    && wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$dpkgArch.asc" \
    && export GNUPGHOME="$(mktemp -d)" \
    && gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
    && gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
    && rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
    && chmod +x /usr/local/bin/gosu \
    && gosu nobody true \
    && rm -rf /usr/share/doc /usr/share/doc-base \
          /usr/share/man /usr/share/locale /usr/share/zoneinfo \
    && rm -rf /tmp/* /var/tmp/* \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY cakeshop*.war ${CAKESHOP_HOME}/cakeshop.war

# Cakeshop data directory is a volume, so it can be persisted and
# survive image upgrades
VOLUME $CAKESHOP_HOME/data

# for main web interface:
EXPOSE 8080

# will be used by attached slave agents:
EXPOSE 8102

# entrypoint script can't use variables, so install to /usr/local/bin
COPY cakeshop.sh /usr/local/bin/cakeshop.sh
ENTRYPOINT ["/bin/tini", "--", "/usr/local/bin/cakeshop.sh"]
