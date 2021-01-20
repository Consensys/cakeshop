package com.jpmorgan.cakeshop.service.task;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.repo.NodeInfoRepository;
import com.jpmorgan.cakeshop.model.NodeInfo;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.apache.commons.collections4.IterableUtils;
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

    private final NodeInfoRepository nodeInfoRepository;

    private final ObjectMapper jsonMapper;

    @Autowired
    public InitializeNodesTask(GethHttpService gethHttpService, NodeInfoRepository nodeInfoRepository, ObjectMapper jsonMapper) {
        this.gethHttpService = gethHttpService;
        this.nodeInfoRepository = nodeInfoRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run() {
        List<NodeInfo> nodeInfoList = IterableUtils.toList(nodeInfoRepository.findAll());
        if(!nodeInfoList.isEmpty()) {
            gethHttpService.connectToNode(nodeInfoList.get(0).id);
        } else if (StringUtils.isNotEmpty(initialNodesFile)) {
            try {
                LOG.info("Loading initial nodes from: {}", initialNodesFile);
                List<NodeInfo> nodes = jsonMapper
                    .readValue(new File(initialNodesFile), new TypeReference<List<NodeInfo>>() {
                    });
                nodeInfoRepository.saveAll(nodes);
                gethHttpService.connectToNode(nodes.get(0).id);
            } catch (IOException e) {
                LOG.error("Could not load initial nodes file", e);
            }
        }
    }

}
