package com.jpmorgan.cakeshop.service;

/**
 *
 * @author Samer Falah
 */
public interface GethRpcConstants {

    public static final String ADMIN_PEERS = "admin_peers";
    public static final String ADMIN_PEERS_ADD = "admin_addPeer";

    public static final String ADMIN_NODE_INFO = "admin_nodeInfo";
    public static final String ADMIN_VERBOSITY = "debug_verbosity";
    public static final String ADMIN_DATADIR = "admin_datadir";

    public static final String ADMIN_MINER_START = "miner_start";
    public static final String ADMIN_MINER_STOP = "miner_stop";
    public static final String ADMIN_MINER_MINING = "eth_mining";

    public static final String ADMIN_NET_PEER_COUNT = "net_peerCount";
    public static final String ADMIN_ETH_BLOCK_NUMBER = "eth_blockNumber";
    public static final String ADMIN_TXPOOL_STATUS = "txpool_status";

    public static final String PERSONAL_LIST_ACCOUNTS = "personal_listAccounts";
    public static final String PERSONAL_GET_ACCOUNT_BALANCE = "eth_getBalance";

}
