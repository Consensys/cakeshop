package com.jpmorgan.cakeshop.manager.controller;

import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;
import com.jpmorgan.cakeshop.manager.db.entity.RemoteNode;
import com.jpmorgan.cakeshop.manager.service.NodeManagerService;
import com.jpmorgan.cakeshop.manager.service.SaveNodeService;
import com.jpmorgan.cakeshop.manager.utils.Utils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/node", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class SaveNodeController {

    @Autowired
    private SaveNodeService service;

    @Autowired
    private NodeManagerService nodeService;

    @RequestMapping({"/db/save"})
    protected Boolean saveRemoteNode(@RequestBody RemoteNode node) throws URISyntaxException {
        URI uri = new URI(node.getNodeAddress());
        node.setId(uri.getUserInfo());
        if (StringUtils.isNotBlank(node.getCred2())) {
            node.setCred2(Base64.encodeBase64String(node.getCred2().getBytes()));
        }
        service.insert(node);
        return true;
    }

    @RequestMapping({"/db/update"})
    protected Boolean updateRemoteNode(@RequestBody RemoteNode node) {
        if (StringUtils.isNotBlank(node.getCred2())) {
            node.setCred2(Base64.encodeBase64String(node.getCred2().getBytes()));
        }
        service.update(node);
        return true;
    }

    @RequestMapping({"/db/list"})
    protected List<RemoteNode> listRemoteNodes() {
        return service.getRemoteNodesList();
    }

    @RequestMapping({"/db/node"})
    protected RemoteNode getRemoteNode(@RequestBody RemoteNode node) {
        return service.getNode(node.getId());
    }

    @RequestMapping({"/cluster/setup"})
    protected Boolean setupCluster() {

        Boolean success = false;
        final List<RemoteNode> nodes = service.getRemoteNodesList();

        for (RemoteNode node : nodes) {
            for (RemoteNode otherNode : getOtherNodes(nodes, node.getUrl())) {
                String cred2 = null;
                if (StringUtils.isNotBlank(otherNode.getCred2())) {
                    cred2 = new String(Base64.decodeBase64(otherNode.getCred2()));
                }
                if (!node.isClustered()) {
                    success = setupCluster(node.getUrl(), otherNode.getNodeAddress(), otherNode.getConstellationUrl(), otherNode.getCred1(), cred2);
                    //make node clustered
                    node.setIsClustered(Boolean.TRUE);
                    service.update(node);
                } else if (node.isClustered() && !otherNode.isClustered()) {
                    success = setupCluster(node.getUrl(), otherNode.getNodeAddress(), otherNode.getConstellationUrl(), otherNode.getCred1(), cred2);
                }
            }
        }
        return success;
    }

    private List<RemoteNode> getOtherNodes(List<RemoteNode> nodes, String currentNodeUrl) {
        Function<String, Function<List<RemoteNode>, List<RemoteNode>>> otherNodesFunction = Utils::otherNodes;
        return otherNodesFunction.apply(currentNodeUrl).apply(nodes);
    }

    private Boolean setupCluster(String currentNodeUrl, String otherNodeAddress, String constellationUrl, String cred1, String cred2) {

        //add remote node
        Boolean success = false;
        NodeUpdateCommand command = new NodeUpdateCommand();

        if (StringUtils.isNotBlank(otherNodeAddress)) {
            command.address(otherNodeAddress);
            APIResponse<APIData<SimpleResult>, Boolean> result = nodeService.addPeer(currentNodeUrl, cred1, cred2, command);
            success = result.getErrors() == null || result.getErrors().isEmpty();
        }

        //add remote node constellation
        if (StringUtils.isNotBlank(constellationUrl)) {
            command = new NodeUpdateCommand();
            command.constellationNode(constellationUrl);
            APIResponse<APIData<Node>, Node> result = nodeService.addConstellation(currentNodeUrl, cred1, cred2, command);
            success = result.getErrors() == null || result.getErrors().isEmpty();
        }

        return success;
    }

}
