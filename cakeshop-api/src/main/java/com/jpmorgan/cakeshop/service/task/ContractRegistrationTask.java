package com.jpmorgan.cakeshop.service.task;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.TransactionService;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ContractRegistrationTask implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContractRegistrationTask.class);

    @Autowired
    private ContractRegistryService contractRegistry;

    @Autowired
    private TransactionService transactionService;

    @Value("${contract.poll.delay.millis}")
    private Long pollDelayMillis;

    private final TransactionResult transactionResult;
    private final Contract contract;

    @Autowired
    @Qualifier("cacheManager")
    CacheManager cacheManager;

    public ContractRegistrationTask(Contract contract, TransactionResult transactionResult) {
        this.contract = contract;
        this.transactionResult = transactionResult;
    }

    @Override
    public void run() {

        Transaction tx = null;

        LOG.debug("Waiting for contract to be comitted");

        while (tx == null) {
            try {
                tx = transactionService.waitForTx(
                        transactionResult, pollDelayMillis, TimeUnit.MILLISECONDS);

            } catch (APIException e) {
                LOG.warn("Error while waiting for contract to mine", e);
                // TODO add backoff delay if server is down?
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting for contract to mine", e);
                return;
            }
        }

        try {
            contract.setAddress(tx.getContractAddress());
            LOG.info("Registering newly mined contract at address " + contract.getAddress());
            TransactionResult regTx = contractRegistry.register(contract.getOwner(), tx.getContractAddress(), contract.getName(), contract.getABI(),
                    contract.getCode(), contract.getCodeType(), contract.getCreatedDate());

            if (regTx == null) {
                return;
            }

            // invalidate cache

            try {
                transactionService.waitForTx(regTx, pollDelayMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while waiting for registration tx to mine", e);
                return;
            }

            cacheManager.getCache("contracts").evict(contract.getAddress());

        } catch (APIException e) {
            LOG.warn("Failed to register contract at address " + tx.getContractAddress(), e);
        }

    }
}
