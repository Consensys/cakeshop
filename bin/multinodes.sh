#!/bin/sh

CURRENT=`pwd`
GETH_NODE_PORT=30303
GETH_HTTP_PORT=8102
RAFT_PORT=50401
TM_PORT=9000
CAKESHOP_PORT=8080
NODES=("node1" "node2" "node3")


case "$1" in 
 init)
   for i in "${NODES[@]}"
     do :
         rm -rf "$CURRENT/$i"
         mkdir -p "$CURRENT/$i"
         cp -n cakeshop.war "$CURRENT/$i"
         cd "$CURRENT/$i"
         echo "Initializing $i"
         java -Dserver.port=$CAKESHOP_PORT -Dgeth.url=http://localhost:$GETH_HTTP_PORT -Dgeth.node.port=$GETH_NODE_PORT -Dgeth.raft.port=$RAFT_PORT -Dgeth.transaction_manager.url=http://127.0.0.1:$TM_PORT  -jar cakeshop.war init > /dev/null
         echo "done"
         GETH_HTTP_PORT=$(( GETH_HTTP_PORT+1 ))
         GETH_NODE_PORT=$(( GETH_NODE_PORT+1 ))
         RAFT_PORT=$(( RAFT_PORT+1 ))
         TM_PORT=$(( TM_PORT+1 ))
         CAKESHOP_PORT=$(( CAKESHOP_PORT+1 ))
         cd ../
      done
 ;;

 start)
   for i in "${NODES[@]}"
     do :
         cd "$CURRENT/$i"
         set +x
         nohup java -jar cakeshop.war &>/dev/null &
         set -x
         sleep 5
         cd ../
     done
 ;;
 
 stop)
   pkill -f cakeshop
 ;;

 esac
