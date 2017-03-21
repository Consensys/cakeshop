#!/bin/sh

CURRENT=`pwd`
GETH_NODE_PORT=30303
GETH_HTTP_PORT=8102
CONSTELLATION_PORT=9000
CAKESHOP_PORT=8080
NODES=("node1" "node2" "node3" "node4")


case "$1" in 
 start)
   for i in "${NODES[@]}" 
     do : 
         mkdir -p "$CURRENT/$i" 
         cp -n cakeshop.war "$CURRENT/$i"
         cd "$CURRENT/$i"
         nohup java -Dserver.port=$CAKESHOP_PORT -Dgeth.url=http://localhost:$GETH_HTTP_PORT -Dgeth.node.port=$GETH_NODE_PORT -Dgeth.constellaiton.url=http://127.0.0.1:$CONSTELLATION_PORT  -jar cakeshop.war &>/dev/null &
         sleep 5
         GETH_HTTP_PORT=$(( GETH_HTTP_PORT+1 ))
         GETH_NODE_PORT=$(( GETH_NODE_PORT+1 ))
         CONSTELLATION_PORT=$(( CONSTELLATION_PORT+1 ))
         CAKESHOP_PORT=$(( CAKESHOP_PORT+1 ))
         cd ../
      done
 ;;
 
 stop)
   killall constellation-node
   killall java
 ;;

 esac
