package com.jpmorgan.cakeshop.service.task;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BlockchainInitializerTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainInitializerTask.class);

    @Autowired
    private ContractRegistryService contractRegistry;

    @Value("${contract.registry.addr:}")
    private String contractRegistryAddress;

    @Override
    public void run() {
        try {
            LOG.info("Deploying ContractRegistry to chain");
            contractRegistry.deploy();

        } catch (APIException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }
    }

}
