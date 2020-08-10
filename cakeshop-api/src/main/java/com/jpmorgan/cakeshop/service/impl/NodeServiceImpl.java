package com.jpmorgan.cakeshop.service.impl;

import com.google.common.base.Joiner;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.PeerDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.Peer;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.GethRpcConstants;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.util.AbiUtils;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import com.jpmorgan.cakeshop.util.EEUtils;
import com.jpmorgan.cakeshop.util.EEUtils.IP;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;

@Service
public class NodeServiceImpl implements NodeService, GethRpcConstants {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);
    public static final String STATIC_NODES_JSON = "static-nodes.json";
    public static final String PERMISSIONED_NODES_JSON = "permissioned-nodes.json";

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    private PeerDAO peerDAO;

    private Map<String, Object> lastNodeInfo;

    @Override
    public Node get() throws APIException {

        Node node = new Node();

        Map<String, Object> data = null;

        try {
            //check if node is available
            // TODO we should be able to use a proper AdminNodeInfo object instead of a generic Map,
            // TODO but let's make that change when we switch to using the web3j-quorum library
            data = gethService.executeGethCall(ADMIN_NODE_INFO);
            gethService.setConnected(true);
            lastNodeInfo = data;

            node.setRpcUrl(gethService.getCurrentRpcUrl());

            node.setId((String) data.get("id"));
            node.setStatus(StringUtils.isEmpty((String) data.get("id")) ? NODE_NOT_RUNNING_STATUS : NODE_RUNNING_STATUS);
            node.setNodeName((String) data.get("name"));
            node.setConsensus(getConsensusType());

            // populate enode and url/ip
            String nodeURI = (String) data.get("enode");
            if (StringUtils.isNotEmpty(nodeURI)) {
                try {
                    URI uri = new URI(nodeURI);
                    String host = uri.getHost();
                    // if host or IP aren't set, then populate with correct IP
                    if (StringUtils.isEmpty(host) || "[::]".equals(host) || "0.0.0.0".equalsIgnoreCase(host)) {

                        try {
                            List<IP> ips = EEUtils.getAllIPs();
                            uri = new URI(uri.getScheme(), uri.getUserInfo(), ips.get(0).getAddr(), uri.getPort(), null, uri.getQuery(), null);
                            node.setNodeUrl(uri.toString());
                            node.setNodeIP(Joiner.on(",").join(ips));

                        } catch (APIException ex) {
                            LOG.error(ex.getMessage());
                            node.setNodeUrl(nodeURI);
                            node.setNodeIP(host);
                        }

                    } else {
                        node.setNodeUrl(nodeURI);
                    }
                } catch (URISyntaxException ex) {
                    LOG.error(ex.getMessage());
                    throw new APIException(ex.getMessage());
                }
            }

            data = gethService.executeGethCall(ADMIN_MINER_MINING);
            Boolean mining = (Boolean) data.get(SIMPLE_RESULT);
            node.setMining(mining == null ? false : mining);

            // peer count
            data = gethService.executeGethCall(ADMIN_NET_PEER_COUNT);
            String peerCount = (String) data.get(SIMPLE_RESULT);
            node.setPeerCount(peerCount == null ? 0 : Integer.decode(peerCount));

            // get last block number
            data = gethService.executeGethCall(ADMIN_ETH_BLOCK_NUMBER);
            String blockNumber = (String) data.get(SIMPLE_RESULT);
            node.setLatestBlock(blockNumber == null ? 0 : Integer.decode(blockNumber));

            // get pending transactions
            data = gethService.executeGethCall(ADMIN_TXPOOL_STATUS);
            Integer pending = AbiUtils.hexToBigInteger((String) data.get("pending")).intValue();
            node.setPendingTxn(pending == null ? 0 : pending);

            if (isRaft()) {
                // get raft role
                data = gethService.executeGethCall(RAFT_ROLE);
                node.setRole((String) data.get(SIMPLE_RESULT));
            }

            node.setPeers(peers());

        } catch (APIException ex) {
            gethService.setConnected(false);
            Throwable cause = ex.getCause();
            if (cause instanceof ResourceAccessException) {
                node.setStatus(NODE_NOT_RUNNING_STATUS);
                return node;
            }

            throw ex;

        } catch (NumberFormatException ex) {
            gethService.setConnected(false);
            LOG.error(ex.getMessage());
            throw new APIException(ex.getMessage());
        }

        return node;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Peer> peers() throws APIException {
        Map<String, Object> data = gethService.executeGethCall(ADMIN_PEERS);
        Map<String, Peer> peerList = new HashMap<>();
        if (data != null) {
            List<Map<String, Object>> peers = (List<Map<String, Object>>) data.get(SIMPLE_RESULT);
            if (peers != null) {
                for (Map<String, Object> peerMap : peers) {
                    Peer peer = createPeer(peerMap);
                    peerList.put(peer.getId(), peer);
                }
            }

        }

        // peers doesn't include self normally, add it
        Peer self = createPeer(lastNodeInfo);
        self.setNodeName("Self");
        peerList.put(self.getId(), self);

        if (isRaft()) {
            List<Map<String, Object>> raftPeers = (List<Map<String, Object>>) gethService
                .executeGethCall(RAFT_CLUSTER).get(SIMPLE_RESULT);
            if (raftPeers != null) {
                for (Map<String, Object> raftPeer : raftPeers) {
                    String id = (String) raftPeer.get("nodeId");
                    Peer peer;
                    if (!peerList.containsKey(id)) {
                        peerList.put(id, new Peer());
                    }

                    peer = peerList.get(id);
                    peer.setId(id);
                    peer.setRaftId(String.valueOf(raftPeer.get("raftId")));
                    peer.setRole(String.valueOf(raftPeer.get("role")));
                    String nodeUrl = CakeshopUtils.formatEnodeUrl(id,
                        (String) raftPeer.get("ip"),
                        String.valueOf(raftPeer.get("p2pPort")),
                        String.valueOf(raftPeer.get("raftPort")));
                    LOG.info(nodeUrl);
                    peer.setNodeUrl(nodeUrl);
                }
            }
        }

        ArrayList<Peer> peers = new ArrayList<>(peerList.values());
        peers.sort(Comparator.comparing(Peer::getRaftId));

        return peers;
    }

    @Override
    public boolean addPeer(String address) throws APIException {

        URI uri = null;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }

        if (isRaft()) {
            Map<String, Object> res = gethService.executeGethCall(RAFT_ADD_PEER, address);
            if (res == null) {
                throw new APIException("Could not add raft peer: " + address);
            }
        }

        Map<String, Object> res = gethService.executeGethCall(ADMIN_PEERS_ADD, address);
        if (res == null) {
            throw new APIException("Could not add geth peer: " + address);
        }

        boolean added = (boolean) res.get(SIMPLE_RESULT);

        if (added) {

            Peer peer = new Peer();
            peer.setId(uri.getUserInfo());
            peer.setNodeIP(uri.getHost());
            peer.setNodeUrl(address);
            peerDAO.save(peer);
            // TODO if db is not enabled, save peers somewhere else? props file?

        }

        return added;
    }

    @Override
    public boolean removePeer(String address) throws APIException {

        URI uri = null;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }

        if (isRaft()) {
            LOG.info("Attempting to remove raft peer");
            List<Peer> raftPeers = peers();
            for (Peer raftPeer : raftPeers) {
                if (raftPeer.getNodeUrl().equals(address)) {
                    LOG.info("Found raft peer at id: {}, removing.", raftPeer.getRaftId());
                    Map<String, Object> res = gethService
                        .executeGethCall(RAFT_REMOVE_PEER, Integer.valueOf(raftPeer.getRaftId()));
                    if (res == null) {
                        throw new APIException("Could not remove raft peer: " + address);
                    }
                }
            }
        }

        Map<String, Object> res = gethService.executeGethCall(ADMIN_PEERS_REMOVE, address);
        if (res == null) {
            throw new APIException("Could not remove geth peer: " + address);
        }

        boolean removed = (boolean) res.get(SIMPLE_RESULT);

        if (removed) {
            Peer peerInDb = peerDAO.getById(uri.getUserInfo());
            if (peerInDb != null) {
                peerDAO.delete(peerInDb);
            }

        }

        return removed;
    }

    @SuppressWarnings("unchecked")
    private Peer createPeer(Map<String, Object> data) {
        Peer peer = new Peer();
        if (data == null || data.isEmpty()) {
            peer.setStatus("down");
            return peer;
        }

        peer.setStatus("running");
        peer.setNodeName((String) data.get("name"));
        peer.setRaftId("0");

        try {
            URI uri = new URI((String) data.get("enode"));
            LOG.info(uri.toString());
            peer.setNodeUrl(uri.toString());
            LOG.info(uri.getHost());
            peer.setNodeIP(uri.getHost());
            LOG.info(uri.getUserInfo());
            peer.setId(uri.getUserInfo());

        } catch (URISyntaxException ex) {
            LOG.error("error parsing Peer Address ", ex.getMessage());
            peer.setNodeUrl("");
        }

        return peer;
    }

    private boolean isRaft() {
        return "raft".equals(getConsensusType());
    }

    private String getConsensusType() {
        // this check will get better when we use a proper AdminNodeInfo object rather than a Map
        String consensus = "unknown";
        try {
            Map<String, Object> protocols = (Map<String, Object>) lastNodeInfo.get("protocols");
            if(protocols.containsKey("istanbul")) {
                consensus = "istanbul";
            } else {
                Map<String, Object> eth = (Map<String, Object>) protocols.get("eth");
                consensus = (String) eth.get("consensus");
            }
        } catch (Exception e) {
            LOG.debug("Could not retrieve consensus type from admin_nodeInfo", e);
        }
        return consensus;
    }
}
