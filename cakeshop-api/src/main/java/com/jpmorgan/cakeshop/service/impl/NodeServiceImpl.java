package com.jpmorgan.cakeshop.service.impl;

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
import org.web3j.quorum.methods.response.ConsensusNoResponse;
import org.web3j.quorum.methods.response.istanbul.IstanbulCandidates;
import org.web3j.quorum.methods.response.istanbul.IstanbulNodeAddress;
import org.web3j.quorum.methods.response.istanbul.IstanbulValidators;
import org.web3j.protocol.admin.methods.response.BooleanResponse;
import org.web3j.protocol.besu.response.BesuEthAccountsMapResponse;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthAccounts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;

@Service
public class NodeServiceImpl implements NodeService, GethRpcConstants {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

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

            String currentRpcUrl = gethService.getCurrentRpcUrl();
            node.setRpcUrl(currentRpcUrl);

            node.setId((String) data.get("id"));
            node.setStatus(StringUtils.isEmpty((String) data.get("id")) ? NODE_NOT_RUNNING_STATUS : NODE_RUNNING_STATUS);
            node.setNodeName((String) data.get("name"));
            node.setConsensus(getConsensusType());

            String nodeURI = (String) data.get("enode");

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

            try {
                data = gethService.executeGethCall(ADMIN_MINER_MINING);
                Boolean mining = (Boolean) data.get(SIMPLE_RESULT);
                node.setMining(mining == null ? false : mining);
            } catch (APIException ex) {
                // allow other calls to carry on and collect partial state such
                // as in cases when some RPC calls aren't supported
                if (ex.getCause() != null) {
                    throw ex;
                }
            }

            try {
                // peer count
                data = gethService.executeGethCall(ADMIN_NET_PEER_COUNT);
                String peerCount = (String) data.get(SIMPLE_RESULT);
                node.setPeerCount(peerCount == null ? 0 : Integer.decode(peerCount));
            } catch (APIException ex) {
                // allow other calls to carry on and collect partial state such
                // as in cases when some RPC calls aren't supported
                if (ex.getCause() != null) {
                    throw ex;
                }
            }

            try {
                // get last block number
                data = gethService.executeGethCall(ADMIN_ETH_BLOCK_NUMBER);
                String blockNumber = (String) data.get(SIMPLE_RESULT);
                node.setLatestBlock(blockNumber == null ? 0 : Integer.decode(blockNumber));
            } catch (APIException ex) {
                // allow other calls to carry on and collect partial state such
                // as in cases when some RPC calls aren't supported
                if (ex.getCause() != null) {
                    throw ex;
                }
            }

            try {
                // get pending transactions
                data = gethService.executeGethCall(ADMIN_TXPOOL_STATUS);
                Integer pending = AbiUtils.hexToBigInteger((String) data.get("pending")).intValue();
                node.setPendingTxn(pending == null ? 0 : pending);
            } catch (APIException ex) {
                // allow other calls to carry on and collect partial state such
                // as in cases when some RPC calls aren't supported
                if (ex.getCause() != null) {
                    throw ex;
                }
            }

            if (isRaft()) {
                try {
                    // get raft role
                    data = gethService.executeGethCall(RAFT_ROLE);
                    node.setRole((String) data.get(SIMPLE_RESULT));
                } catch (APIException ex) {
                    // allow other calls to carry on and collect partial state such
                    // as in cases when some RPC calls aren't supported
                    if (ex.getCause() != null) {
                        throw ex;
                    }
                }
            }

            try {
                node.setPeers(peers());
            } catch (APIException ex) {
                // allow other calls to carry on and collect partial state such
                // as in cases when some RPC calls aren't supported
                if (ex.getCause() != null) {
                    throw ex;
                }
            }

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
            String raftLeader = ((String) gethService.executeGethCall(RAFT_LEADER)
                .get(SIMPLE_RESULT));
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
                    peer.setRaftId((Integer) raftPeer.get("raftId"));
                    peer.setRole(String.valueOf(raftPeer.get("role")));
                    String nodeUrl = CakeshopUtils.formatEnodeUrl(id,
                        (String) raftPeer.get("hostname"),
                        String.valueOf(raftPeer.get("p2pPort")),
                        String.valueOf(raftPeer.get("raftPort")));
                    peer.setNodeUrl(nodeUrl);
                }
            }
        }

        ArrayList<Peer> peers = new ArrayList<>(peerList.values());
        peers.sort(Comparator.comparingInt(Peer::getRaftId));

        return peers;
    }

    @Override
    public boolean addPeer(String address, boolean raftLearner) throws APIException {

        URI uri = null;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }

        if (isRaft()) {
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
        }

        return added;
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
                Map<String, Object> res = gethService.executeGethCall(RAFT_PROMOTE_TO_PEER, raftPeer.getRaftId());
                if (res == null) {
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
                    Map<String, Object> res = gethService
                        .executeGethCall(RAFT_REMOVE_PEER, raftPeer.getRaftId());
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
        peer.setRaftId(0);

        try {
            URI uri = new URI((String) data.get("enode"));
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

    private String getConsensusType() {
        // this check will get better when we use a proper AdminNodeInfo object rather than a Map
        String consensus = "unknown";
        try {
            Map<String, Object> protocols = (Map<String, Object>) lastNodeInfo.get("protocols");
            if(protocols.containsKey("istanbul")) {
                consensus = "istanbul";
            } else if (protocols.containsKey("eth")){
                Map<String, Object> eth = (Map<String, Object>) protocols.get("eth");
                consensus = (String) eth.get("consensus");
            } else if (protocols.containsKey("clique")) {
            	Map<String, Object> eth = (Map<String, Object>) protocols.get("clique");
                consensus = (String) eth.get("consensus");
            }
        } catch (Exception e) {
            LOG.debug("Could not retrieve consensus type from admin_nodeInfo", e);
        }
        return consensus;
    }

    @Override
    public List<String> getSigners() throws APIException {
    	EthAccounts signers = null;
    	try {
    		signers = gethService.getBesuService().cliqueGetSigners(DefaultBlockParameter.valueOf("latest")).send();
    		if (signers == null || signers.hasError()) {
    			throw new APIException(signers.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return signers.getAccounts();
    }

    @Override
    public Map<String, Boolean> getProposals() throws APIException {
    	BesuEthAccountsMapResponse proposals = null;
    	try {
    		proposals = gethService.getBesuService().cliqueProposals().send();
    		if (proposals == null || proposals.hasError()) {
    			throw new APIException(proposals.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return proposals.getAccounts();
    }

    @Override
    public Boolean cliquePropose(String address, boolean auth) throws APIException {
    	BooleanResponse response = null;
    	try {
    		response = gethService.getBesuService().cliquePropose(address, auth).send();
    		if (response == null || response.hasError()) {
    			throw new APIException(response.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return true;
    }

    @Override
    public Boolean cliqueDiscard(String address) throws APIException {
    	BooleanResponse response = null;
    	try {
    		response = gethService.getBesuService().cliqueDiscard(address).send();
    		if (response == null || response.hasError()) {
    			throw new APIException(response.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return true;
    }

    @Override
    public List<String> getValidators() throws APIException {
    	IstanbulValidators validators = null;
    	try {
    		validators = gethService.getQuorumService().istanbulGetValidators("latest").send();
    		if (validators == null || validators.hasError()) {
    			throw new APIException(validators.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return validators.getValidators();
    }

    @Override
    public Map<String, Boolean> getCandidates() throws APIException {
    	IstanbulCandidates candidates = null;
    	try {
    		candidates = gethService.getQuorumService().istanbulCandidates().send();
    		if (candidates == null || candidates.hasError()) {
    			throw new APIException(candidates.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return candidates.getCandidates();
    }

    @Override
    public String propose(String address, boolean auth) throws APIException {
    	ConsensusNoResponse response = null;
    	try {
    		response = gethService.getQuorumService().istanbulPropose(address, auth).send();
    		if (response == null || response.hasError()) {
    			throw new APIException(response.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return response.getNoResponse();
    }

    @Override
    public String discard(String address) throws APIException {
    	ConsensusNoResponse response = null;
    	try {
    		response = gethService.getQuorumService().istanbulDiscard(address).send();
    		if (response == null || response.hasError()) {
    			throw new APIException(response.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return response.getNoResponse();
    }

    @Override
    public String istanbulGetNodeAddress() throws APIException {
    	IstanbulNodeAddress address = null;
    	try {
    		address = gethService.getQuorumService().istanbulNodeAddress().send();
    		if (address == null || address.hasError()) {
    			throw new APIException(address.getError().getMessage());
    		}
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return address.getNodeAddress();
    }
}
