package com.jpmorgan.cakeshop.service.task;

import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.WalletService;
import java.util.List;
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

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletDAO walletDAO;

    @Value("${contract.registry.addr:}")
    private String contractRegistryAddress;

    @Autowired
    private GethConfig gethConfig;

    @Override
    public void run() {
        try {
            LOG.info("Deploying ContractRegistry to chain");
            contractRegistry.deploy();
            syncWalletDb();

        } catch (APIException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }
    }

    private void syncWalletDb() {

        if (!gethConfig.isDbEnabled()) {
            return;
        }

        LOG.info("Storing existing wallet account balances");
        try {
            List<Account> list = walletService.list();
            for (Account account : list) {
                walletDAO.save(account);
            }
        } catch (APIException e) {
            LOG.error("Error reading local wallet", e);
        }

    }
}
