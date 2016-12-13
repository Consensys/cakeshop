package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.NodeConfig;
import com.jpmorgan.cakeshop.model.Peer;

import java.util.List;

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
     * @param logLevel     Log level (0 = least verbose, 6 = most verbose)
     * @param networkID
     * @param identity
     * @param mining
     * @return
     * @throws APIException
     */
    public NodeConfig update(
            Integer logLevel,
            Integer networkID,
            String identity,
            Boolean mining,
            String extraParams,
            String genesisBlock) throws APIException;

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

}
