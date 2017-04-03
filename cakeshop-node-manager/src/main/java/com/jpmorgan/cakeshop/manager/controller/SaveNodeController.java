package com.jpmorgan.cakeshop.manager.controller;

import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;
import com.jpmorgan.cakeshop.manager.db.entity.RemoteNode;
import com.jpmorgan.cakeshop.manager.service.NodeManagerService;
import com.jpmorgan.cakeshop.manager.service.SaveNodeService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
        service.insert(node);
        return true;
    }

    @RequestMapping({"/db/update"})
    protected Boolean updateRemoteNode(@RequestBody RemoteNode node) {
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

        List<RemoteNode> nodes = service.getRemoteNodesList();
        Boolean success = false;

        for (RemoteNode node : nodes) {
            List<RemoteNode> otherNodes = getOtherNodes(nodes, node.getUrl());
            Boolean updateNode = false;

            for (RemoteNode otherNode : otherNodes) {

                if (!node.isClustered()) {
                    success = setupCluster(node.getUrl(), otherNode.getNodeAddress(), otherNode.getConstellationUrl());
                    //make node clustered
                    node.setIsClustered(Boolean.TRUE);
                    updateNode = true;
                } else if (node.isClustered() && !otherNode.isClustered()) {
                    success = setupCluster(node.getUrl(), otherNode.getNodeAddress(), otherNode.getConstellationUrl());
                }
            }

            if (updateNode) {
                service.update(node);
            }
        }
        return success;
    }

    private List<RemoteNode> getOtherNodes(List<RemoteNode> nodes, String currentNodeUrl) {
        List<RemoteNode> otherNodes = new ArrayList<>();
        nodes.stream().filter((node) -> (!node.getUrl().equals(currentNodeUrl))).forEachOrdered((node) -> {
            otherNodes.add(node);
        });
        return otherNodes;
    }

    private Boolean setupCluster(String currentNodeUrl, String otherNodeAddress, String constellationUrl) {

        //add remote node
        Boolean success = false;
        NodeUpdateCommand command = new NodeUpdateCommand();

        if (StringUtils.isNotBlank(otherNodeAddress)) {
            command.address(otherNodeAddress);
            APIResponse<APIData<SimpleResult>, Boolean> result = nodeService.addPeer(currentNodeUrl, null, null, command);
            success = result.getErrors() == null || result.getErrors().isEmpty();
        }

        //add remote node constellation
        if (StringUtils.isNotBlank(constellationUrl)) {
            command = new NodeUpdateCommand();
            command.constellationNode(constellationUrl);
            APIResponse<APIData<Node>, Node> result = nodeService.addConstellation(currentNodeUrl, null, null, command);
            success = result.getErrors() == null || result.getErrors().isEmpty();
        }

        return success;
    }

}
