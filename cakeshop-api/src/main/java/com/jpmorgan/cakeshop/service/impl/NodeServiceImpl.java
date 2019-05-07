package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.bean.GethRunner;
import com.jpmorgan.cakeshop.bean.TransactionManagerRunner;
import com.jpmorgan.cakeshop.dao.PeerDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.model.NodeConfig;
import com.jpmorgan.cakeshop.model.NodeSettings;
import com.jpmorgan.cakeshop.model.Peer;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.GethRpcConstants;
import com.jpmorgan.cakeshop.service.NodeService;
import com.jpmorgan.cakeshop.util.AbiUtils;
import com.jpmorgan.cakeshop.util.EEUtils;
import com.jpmorgan.cakeshop.util.EEUtils.IP;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jpmorgan.cakeshop.service.impl.GethHttpServiceImpl.SIMPLE_RESULT;

@Service
public class NodeServiceImpl implements NodeService, GethRpcConstants {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    private GethRunner gethRunner;

    @Autowired
    private TransactionManagerRunner transactionManagerRunner;

    @Autowired
    private PeerDAO peerDAO;

    @Override
    public Node get() throws APIException {

        Node node = new Node();

        Map<String, Object> data = null;

        try {
            //check if node is available
            data = gethService.executeGethCall(ADMIN_NODE_INFO);

            node.setRpcUrl(gethConfig.getRpcUrl());
            node.setDataDirectory(gethConfig.getGethDataDirPath());

            node.setId((String) data.get("id"));
            node.setStatus(StringUtils.isEmpty((String) data.get("id")) ? NODE_NOT_RUNNING_STATUS : NODE_RUNNING_STATUS);
            node.setNodeName((String) data.get("name"));

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

            try {
                node.setConfig(createNodeConfig());
            } catch (IOException e) {
                throw new APIException("Failed to read genesis block file", e);
            }

            node.setPeers(peers());

        } catch (APIException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ResourceAccessException) {
                node.setStatus(NODE_NOT_RUNNING_STATUS);
                return node;
            }

            throw ex;

        } catch (NumberFormatException ex) {
            LOG.error(ex.getMessage());
            throw new APIException(ex.getMessage());

        }

        return node;
    }

    private NodeConfig createNodeConfig() throws IOException {
        return new NodeConfig(gethConfig.getIdentity(), gethConfig.isMining(), gethConfig.getNetworkId(),
                gethConfig.getVerbosity(), gethRunner.getGenesisBlock(), gethConfig.getExtraParams());
    }

    @Override
    public NodeConfig update(NodeSettings settings) throws APIException {

        boolean restart = false;
        boolean reset = false;

        if (null != settings) {
            if (settings.getNetworkId() != null && !settings.getNetworkId().equals(gethConfig.getNetworkId())) {
                gethConfig.setNetworkId(settings.getNetworkId());
                restart = true;
            }

            if (StringUtils.isNotEmpty(settings.getIdentity()) && !settings.getIdentity().contentEquals(gethConfig.getIdentity())) {
                gethConfig.setIdentity(settings.getIdentity());
                restart = true;
            }

            if (settings.getLogLevel() != null && !settings.getLogLevel().equals(gethConfig.getVerbosity())) {
                gethConfig.setVerbosity(settings.getLogLevel());
                if (!restart) {
                    // make it live immediately
                    gethService.executeGethCall(ADMIN_VERBOSITY, settings.getLogLevel());
                }
            }

            String currExtraParams = gethConfig.getExtraParams();
            // TODO currently no way to erase all the extra params because an empty string is
            // TODO assumed to be no update to the setting
            if (StringUtils.isNotBlank(settings.getExtraParams()) && (currExtraParams == null || !settings.getExtraParams().contentEquals(currExtraParams))) {
                gethConfig.setExtraParams(settings.getExtraParams());
                restart = true;
            }

            try {
                if (StringUtils.isNotBlank(settings.getGenesisBlock()) && !settings.getGenesisBlock().contentEquals(gethRunner.getGenesisBlock())) {
                    gethRunner.setGenesisBlock(settings.getGenesisBlock());
                    reset = true;
                }
            } catch (IOException e) {
                throw new APIException("Failed to update genesis block", e);
            }

            if (settings.isMining() != null && !settings.isMining().equals(gethConfig.isMining())) {
                gethConfig.setMining(settings.isMining());

                if (!restart) {
                    // make it live immediately
                    if (settings.isMining()) {
                        gethService.executeGethCall(ADMIN_MINER_START, 1);
                    } else {
                        gethService.executeGethCall(ADMIN_MINER_STOP);
                    }
                }
            }

        }

        NodeConfig nodeInfo;
        try {
            gethConfig.save();
            nodeInfo = createNodeConfig();
        } catch (IOException e) {
            LOG.error("Error saving config", e);
            throw new APIException("Error saving config", e);
        }

        // TODO reset/restart in background?
        if (reset) {
            gethService.reset();
        } else if (restart) {
            restart();
        }

        return nodeInfo;
    }

    @Override
    public Boolean reset() {
        try {
            gethRunner.initFromVendorConfig();
        } catch (IOException e) {
            LOG.warn("Failed to reset config file", e);
            return false;
        }

        restart();
        return true;
    }

    private void restart() {
        gethService.stop();
        gethService.start();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Peer> peers() throws APIException {
        Map<String, Object> data = gethService.executeGethCall(ADMIN_PEERS);

        if (data == null) {
            return null;
        }

        List<Peer> peerList = new ArrayList<>();
        List<Map<String, Object>> peers = (List<Map<String, Object>>) data.get("_result");
        if (peers != null) {
            for (Map<String, Object> peerMap : peers) {
                Peer peer = createPeer(peerMap);
                peerList.add(peer);
            }
        }

        return peerList;
    }

    @Override
    public boolean addPeer(String address) throws APIException {

        URI uri = null;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            throw new APIException("Bad peer address URI: " + address, e);
        }

        if (gethConfig.isPermissionedNode()) {
            File permissionJson = new File(gethConfig.getGethDataDirPath().concat("/").concat("permissioned-nodes.json"));
            List<String> permissionNodes;
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (!permissionJson.exists()) {
                    permissionJson.createNewFile();
                    permissionNodes = Lists.newArrayList(address);
                    mapper.writeValue(permissionJson, permissionNodes);
                } else {
                    permissionNodes = mapper.readValue(permissionJson, new TypeReference<List<String>>() {
                    });
                    if (!permissionNodes.contains(address)) {
                        permissionNodes.add(address);
                        mapper.writeValue(permissionJson, permissionNodes);
                    }
                }
            } catch (IOException ex) {
                LOG.error("Could not operate with permission file", ex);
                throw new APIException("Could not operate with permission file");
            }
        }

        Map<String, Object> res = gethService.executeGethCall(ADMIN_PEERS_ADD, address);
        if (res == null) {
            return false;
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
    public Map<String, Object> getTransactionManagerNodes() {
        Map<String, Object> nodeMap = new LinkedHashMap<>();
        nodeMap.put("local", gethConfig.getGethTransactionManagerUrl());
        nodeMap.put("remote", gethConfig.getGethTransactionManagerPeers());
        return nodeMap;
    }

    @Override
    public NodeConfig addTransactionManagerNode(String node) throws APIException {
        NodeConfig nodeInfo;
        try {
            List<String> nodes = gethConfig.getGethTransactionManagerPeers();
            nodes.add(node);
            gethConfig.setGethTransactionManagerPeers(nodes);
            gethConfig.save();
            transactionManagerRunner.writeTransactionManagerConfig();
            nodeInfo = createNodeConfig();
            restart();
            return nodeInfo;
        } catch (IOException e) {
            LOG.error("Error saving transaction manager config", e);
            throw new APIException("Error saving transaction manager config", e);
        }
    }

    @Override
    public NodeConfig removeTransactionManagerNode(String transactionManagerNode)
        throws APIException {
        NodeConfig nodeInfo;
        try {
            List<String> nodes = gethConfig.getGethTransactionManagerPeers();
            boolean wasInList = nodes.remove(transactionManagerNode);
            if (!wasInList) {
                throw new IOException("Peer node was not in list");
            }
            gethConfig.setGethTransactionManagerPeers(nodes);
            gethConfig.save();
            transactionManagerRunner.writeTransactionManagerConfig();
            restart();
            nodeInfo = createNodeConfig();
            return nodeInfo;
        } catch (IOException e) {
            LOG.error("Error saving transaction manager config", e);
            throw new APIException("Error saving transaction manager config", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Peer createPeer(Map<String, Object> data
    ) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        Peer peer = new Peer();
        peer.setStatus("running");
        peer.setId((String) data.get("id"));
        peer.setNodeName((String) data.get("name"));

        try {
            String remoteAddress = (String) ((Map<String, Object>) data.get("network")).get("remoteAddress");
            URI uri = new URI("enode://" + peer.getId() + "@" + remoteAddress);
            peer.setNodeUrl(uri.toString());
            peer.setNodeIP(uri.getHost());

        } catch (URISyntaxException ex) {
            LOG.error("error parsing Peer Address ", ex.getMessage());
            peer.setNodeUrl("");
        }

        return peer;
    }
}
