package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.dao.PeerDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.Peer;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.GethRpcConstants;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.util.AbiUtils;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;
import org.web3j.protocol.core.methods.response.admin.*;
import org.web3j.protocol.core.methods.response.admin.AdminNodeInfo.NodeInfo;
import org.web3j.quorum.methods.response.raft.RaftPeer;

@Service
public class NodeServiceImpl implements NodeService, GethRpcConstants {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private PeerDAO peerDAO;

    private NodeInfo lastNodeInfo;

    @Override
    public Node get() throws APIException {

        Node node = new Node();

        NodeInfo info = null;

        try {
            //check if node is available
            // TODO we should be able to use a proper AdminNodeInfo object instead of a generic Map,
            // TODO but let's make that change when we switch to using the web3j-quorum library
            
        	info = gethService.getQuorumService().adminNodeInfo().send().getResult();
            gethService.setConnected(true);
            lastNodeInfo = info;

            String currentRpcUrl = gethService.getCurrentRpcUrl();
            node.setRpcUrl(currentRpcUrl);

            node.setId(info.getId());
            node.setStatus(StringUtils.isEmpty(info.getId()) ? NODE_NOT_RUNNING_STATUS : NODE_RUNNING_STATUS);
            node.setNodeName(info.getName());
            node.setConsensus(getConsensusType());

            String nodeURI = info.getEnode();

            if (StringUtils.isNotEmpty(nodeURI)) {
                // nodeURI will have the internal ip of the node, which may be different than the ip used to connect.
                // switch the host to whatever RPC url/ip we used to connect to the node
                try {
                    String currentRpcHost = new URI(currentRpcUrl).getHost();
                    if(currentRpcHost.equals("localhost")) {
                        // raft doesn't like 'localhost', use standard localhost ip instead
                        currentRpcHost = "127.0.0.1";
                    }
                    String fixedNodeUri = UriComponentsBuilder.fromUriString(nodeURI)
                        .host(currentRpcHost)
                        .build()
                        .toUriString();
                    node.setNodeUrl(fixedNodeUri);
                } catch (URISyntaxException ex) {
                    LOG.error(ex.getMessage());
                    throw new APIException(ex.getMessage());
                }
            }

            Boolean mining = gethService.getQuorumService().ethMining().send().isMining();
            node.setMining(mining == null ? false : mining);

            // peer count
            BigInteger peerCount = gethService.getQuorumService().netPeerCount().send().getQuantity();
            node.setPeerCount(peerCount == null ? BigInteger.ZERO : peerCount);

            // get last block number
            BigInteger blockNumber = gethService.getQuorumService().ethBlockNumber().send().getBlockNumber();
            node.setLatestBlock(blockNumber == null ? BigInteger.ZERO : blockNumber);

            // get pending transactions
            Integer pending = AbiUtils.hexToBigInteger((String) gethService.executeGethCall(ADMIN_TXPOOL_STATUS).get("pending")).intValue();
            node.setPendingTxn(pending == null ? 0 : pending);

            if (isRaft()) {
                // get raft role
                node.setRole(gethService.getQuorumService().raftGetRole().send().getRole());
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
        } catch (IOException ex) {
        	gethService.setConnected(false);
        	LOG.error(ex.getMessage());
        	throw new APIException(ex.getMessage());
        }

        return node;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Peer> peers() throws APIException {
    	try {
    		List<org.web3j.protocol.core.methods.response.admin.AdminPeers.Peer> adminPeers = gethService.getQuorumService().adminPeers().send().getResult();
    		Map<String, Peer> peerList = new HashMap<>();
    		if (adminPeers != null) {
    			for (org.web3j.protocol.core.methods.response.admin.AdminPeers.Peer peer : adminPeers) {
    				Peer p = new Peer();
    				p.setId(peer.getId());
    				p.setNodeName(peer.getName());
    				p.setStatus(StringUtils.isEmpty(peer.getId()) ? NODE_NOT_RUNNING_STATUS : NODE_RUNNING_STATUS);
    				p.setRaftId(0);
//        		URI uri = new URI(peer.getEnode());
//                p.setNodeUrl(uri.toString());
//                p.setNodeIP(uri.getHost());
//                p.setId(uri.getUserInfo());
    				peerList.put(p.getId(), p);
    			}
    		}

    		// peers doesn't include self normally, add it
    		Peer self = createPeer(lastNodeInfo);
    		self.setNodeName("Self");
    		peerList.put(self.getId(), self);

    		if (isRaft()) {
    			List<RaftPeer> raftPeers = null;

        		raftPeers = gethService.getQuorumService().raftGetCluster().send().getCluster().get();
        		if (raftPeers != null) {
        			for (RaftPeer raftPeer : raftPeers) {
        				String id = raftPeer.getNodeId();
        				Peer peer;
        				if (!peerList.containsKey(id)) {
        					peerList.put(id, new Peer());
        				}

        				peer = peerList.get(id);
        				peer.setId(id);
        				peer.setRaftId(Integer.parseInt(raftPeer.getRaftId()));
        				peer.setRole(raftPeer.getRole());
        				String nodeUrl = CakeshopUtils.formatEnodeUrl(id,
        						raftPeer.getHostname(),
        						raftPeer.getP2pPort(),
        						raftPeer.getRaftPort());
        				peer.setNodeUrl(nodeUrl);
        			}
        		}
    		}
       
    		ArrayList<Peer> peers = new ArrayList<>(peerList.values());
    		peers.sort(Comparator.comparingInt(Peer::getRaftId));

    		return peers;
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    }

    @Override
    public BigInteger addPeer(String address, boolean raftLearner) throws APIException {

        URI uri = null;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }
        
        BigInteger addedPeer = BigInteger.ZERO;

        if (isRaft()) {
        	try {
        		addedPeer = raftLearner
        			? gethService.getQuorumService().raftAddPeer(address).send().getAddedPeer()
        			: gethService.getQuorumService().raftAddLearner(address).send().getAddedPeer();
        	} catch (IOException e) {
        		throw new APIException(e.getMessage());
        	}
            String method = raftLearner ? RAFT_ADD_LEARNER : RAFT_ADD_PEER;
            Map<String, Object> res = gethService.executeGethCall(method, address);
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
        }

        return addedPeer;
    }

    @Override
    public void promoteToPeer(String address) throws APIException {
        if (!isRaft()) {
            throw new APIException("Peers may only be promoted in a raft network");
        }

        try {
            new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }

        List<Peer> raftPeers = peers();
        for (Peer raftPeer : raftPeers) {
            if (raftPeer.getNodeUrl().equals(address)) {
                LOG.info("Attempting to promote learner to peer {} {}", raftPeer.getRaftId(), address);
                boolean success = false;
                try {
                	success = gethService.getQuorumService().raftPromoteToPeer(raftPeer.getRaftId()).send().getPromotionStatus();
                } catch (IOException e) {
                	throw new APIException(e.getMessage());
                }
                if (!success) {
                    throw new APIException("Could not promote raft peer: " + address);
                }
                return;
            }
        }
        throw new APIException("Could not find raft peer: " + address);
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
                    String res = null;
                    try {
                    	res = gethService.getQuorumService().raftRemovePeer(raftPeer.getRaftId()).send().getNoResponse();
                    } catch (IOException e) {
                    	throw new APIException(e.getMessage());
                    }
                    if (res == null || res != "success") {
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
    private Peer createPeer(NodeInfo data) {
        Peer peer = new Peer();
        if (data == null || StringUtils.isEmpty(data.getId())) {
            peer.setStatus("down");
            return peer;
        }

        peer.setStatus("running");
        peer.setNodeName(data.getName());
        peer.setRaftId(0);

        try {
            URI uri = new URI(data.getEnode());
            peer.setNodeUrl(uri.toString());
            peer.setNodeIP(uri.getHost());
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

    private String getConsensusType(){
        // need to do some modifications to web3j AdminNodeInfo for this to be nicer
        String consensus = "unknown";
        try {
        	Map<String, Object> data = gethService.executeGethCall(ADMIN_NODE_INFO);
            Map<String, Object> protocols = (Map<String, Object>) data.get("protocols");
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
