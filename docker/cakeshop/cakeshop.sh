#!/bin/bash

set -e

: ${CAKESHOP_HOME:="/opt/cakeshop"}
: ${JAVA_OPTS:=""}

if [[ -n "$CAKESHOP_USER" && -n "$CAKESHOP_GROUP" ]]; then
  chown -R $CAKESHOP_USER:$CAKESHOP_GROUP $CAKESHOP_HOME
  export USER="$CAKESHOP_USER" # spring boot fix
fi

cd $CAKESHOP_HOME

# if `docker run` first argument start with `--` the user is passing cakeshop launcher arguments
if [[ $# -lt 1 ]] || [[ "$1" == "--"* ]]; then
  # read JAVA_OPTS into array
  java_opts_array=()
  while IFS= read -r -d '' item; do
    java_opts_array+=( "$item" )
  done < <([[ $JAVA_OPTS ]] && xargs printf '%s\0' <<<"$JAVA_OPTS")

  exec gosu $CAKESHOP_USER java "${java_opts_array[@]}" -jar ${CAKESHOP_HOME}/cakeshop.war "$@"
fi

# As argument is not cakeshop, assume user want to run her own process,
# for example a `bash` shell to explore this image
exec "$@"
