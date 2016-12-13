NUM_TXNS=$1
if [[ -z "$NUM_TXNS" ]]; then
  NUM_TXNS=10
fi
geth-resources/bin/mac/geth --datadir ~/.ethereum --exec "for(i=0;i<$NUM_TXNS;i++){ eth.sendTransaction({from:eth.coinbase,to:eth.accounts[1],value:1}) }" attach rpc:http://127.0.0.1:8102

