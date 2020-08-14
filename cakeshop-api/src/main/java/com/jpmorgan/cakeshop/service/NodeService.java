package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.Peer;

import java.math.BigInteger;
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
     * Retrieve a list of connected peers
     *
     * @return
     * @throws APIException
     */
    public List<Peer> peers() throws APIException;

    /**
     * Add a node to the raft cluster (optionally make it a view-only learner node)
     *
     * @param address
     * @param raftLearner
     * @return
     * @throws APIException
     */
    public BigInteger addPeer(String address, boolean raftLearner) throws APIException;

    /**
     * Promote raft learner node to full peer
     *
     * @param address
     * @return
     * @throws APIException
     */
    void promoteToPeer(String address) throws APIException;

    /**
     * Remove the given peer
     *
     * @param address
     * @return
     * @throws APIException
     */
    boolean removePeer(String address) throws APIException;

}
