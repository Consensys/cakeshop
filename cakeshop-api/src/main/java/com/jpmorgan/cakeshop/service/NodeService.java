package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.NodeConfig;
import com.jpmorgan.cakeshop.model.NodeSettings;
import com.jpmorgan.cakeshop.model.Peer;
import java.util.List;
import java.util.Map;

public interface NodeService {

    public static final String NODE_RUNNING_STATUS = "running";
    public static final String NODE_NOT_RUNNING_STATUS = "stopped";

    /**
     * Get node information
     *
     * @return {@link Node}
     * @throws APIException
     */
    public Node get() throws APIException;

    /**
     * Update node configuration (may trigger restart)
     *
     * @param settings Log level (0 = least verbose, 6 = most verbose)
     * @return
     * @throws APIException
     */
    public NodeConfig update(
            NodeSettings settings) throws APIException;

    /**
     * Reset node back to default configuration (will restart)
     *
     * @return
     */
    public Boolean reset();

    /**
     * Retrieve a list of connected peers
     *
     * @return
     * @throws APIException
     */
    public List<Peer> peers() throws APIException;

    /**
     * Connect to the given peer
     *
     * @param address
     * @return
     * @throws APIException
     */
    public boolean addPeer(String address) throws APIException;

    /**
     * Remove the given peer
     *
     * @param address
     * @return
     * @throws APIException
     */
    boolean removePeer(String address) throws APIException;

    /**
     * Get list of transaction manager nodes
     *
     * @return
     * @throws APIException
     */
    public Map<String, Object> getTransactionManagerNodes() throws APIException;

    /**
     * Add new transaction manager node
     *
     * @param transactionManagerNode
     * @return
     * @throws APIException
     */
    public NodeConfig addTransactionManagerNode(String transactionManagerNode) throws APIException;

    /**
     * Remove transaction manager node from the list
     *
     * @param transactionManagerNode
     * @return
     * @throws APIException
     */
    public NodeConfig removeTransactionManagerNode(String transactionManagerNode) throws APIException;

}
