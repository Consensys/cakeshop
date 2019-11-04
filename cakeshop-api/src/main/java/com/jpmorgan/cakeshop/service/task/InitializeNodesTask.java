package com.jpmorgan.cakeshop.service.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.NodeInfoDAO;
import com.jpmorgan.cakeshop.model.NodeInfo;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
@Scope("prototype")
public class InitializeNodesTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
        .getLogger(InitializeNodesTask.class);

    @Value("${cakeshop.initialnodes:}")
    String initialNodesFile;

    private final GethHttpService gethHttpService;

    private final GethConfig gethConfig;

    private final NodeInfoDAO nodeInfoDAO;

    private final ObjectMapper jsonMapper;

    @Autowired
    public InitializeNodesTask(GethHttpService gethHttpService,
        GethConfig gethConfig, NodeInfoDAO nodeInfoDAO,
        ObjectMapper jsonMapper) {
        this.gethHttpService = gethHttpService;
        this.gethConfig = gethConfig;
        this.nodeInfoDAO = nodeInfoDAO;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run() {
        if (StringUtils.isNotEmpty(initialNodesFile)) {
            try {
                if (nodeInfoDAO.list().isEmpty()) {
                    LOG.info("Loading initial nodes from: {}", initialNodesFile);
                    List<NodeInfo> nodes = jsonMapper
                        .readValue(new File(initialNodesFile), new TypeReference<List<NodeInfo>>() {
                        });
                    nodeInfoDAO.save(nodes);
                }
            } catch (IOException e) {
                LOG.error("Could not load initial nodes file", e);
            }
        }
        gethHttpService.connectToNode(gethConfig.getSelectedNode());
    }

}
