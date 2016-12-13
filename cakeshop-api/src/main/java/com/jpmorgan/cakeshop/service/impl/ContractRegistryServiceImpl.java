package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContractRegistryServiceImpl.class);

    private static final String REGISTRY_ABI_FILE =
            "contracts" + File.separator + "ContractRegistry.abi.json";

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Value("${contract.registry.addr:}")
    private String contractRegistryAddress;

    @Autowired
    private GethConfigBean gethConfig;

    private final ContractABI abi;

    public ContractRegistryServiceImpl() throws IOException {
        this.abi = ContractABI.fromJson(FileUtils.readClasspathFile(REGISTRY_ABI_FILE));
    }

    @Override
    public boolean deploy() throws APIException {

        try {
            String code = FileUtils.readClasspathFile("contracts/ContractRegistry.sol");
            TransactionResult txr = contractService.create(null, code, CodeType.solidity, null, null, null, null);
            Transaction tx = transactionService.waitForTx(txr, 200, TimeUnit.MILLISECONDS);
            this.contractRegistryAddress = tx.getContractAddress();
            saveContractRegistryAddress(this.contractRegistryAddress);
            saveSharedNetworkConfig(this.contractRegistryAddress);
            return true;

        } catch (IOException | InterruptedException e) {
            LOG.error("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }

        return false;
    }

    private void saveContractRegistryAddress(String addr) throws APIException {
        try {
            LOG.debug("Storing ContractRegistry address " + addr);
            gethConfig.setProperty("contract.registry.addr", addr);
            gethConfig.save();
        } catch (IOException e) {
            LOG.warn("Unable to update application.properties", e);
            throw new APIException("Unable to update env.properties", e);
        }
    }

    /**
     * Update the shared network config with the given ContractRegistry address
     *
     * @param addr
     */
    private void saveSharedNetworkConfig(String addr) {

        // TODO this is a temp solution to the problem of sharing the ContractRegistry
        // address among multiple Cakeshop nodes running on the same machine.

        File fSharedConfig = CakeshopUtils.getSharedNetworkConfigFile();
        if (fSharedConfig == null) {
            return;
        }

        fSharedConfig.getParentFile().mkdirs();
        Properties props = new Properties();
        props.put("contract.registry.addr", addr);
        try {
            props.store(new FileOutputStream(fSharedConfig), null);
            LOG.info("Wrote ContractRegistry address to shared location " + fSharedConfig.toString());
        } catch (IOException e) {
            LOG.warn("Error writing to shared config file at " + fSharedConfig.toString() + ": " + e.getMessage(), e);
        }

    }

    @Override
    public void updateRegistryAddress(String addr) throws APIException {
        // TODO this is a temp solution to the problem of sharing the ContractRegistry
        // address among multiple Cakeshop nodes running on the same machine.
        this.contractRegistryAddress = addr;
        // TODO save?
    }

    @Override
    public TransactionResult register(String from, String id, String name, String abi, String code, CodeType codeType, Long createdDate) throws APIException {

        if (StringUtils.isBlank(contractRegistryAddress)) {
            LOG.warn("Not going to register contract since ContractRegistry address is null");
            return null; // FIXME return silently because registry hasn't yet been registered
        }

        if (name.equalsIgnoreCase("ContractRegistry")) {
            LOG.debug("Skipping registration for ContractRegistry");
            return null;
        }

        return contractService.transact(
                contractRegistryAddress, this.abi, from,
                "register",
                new Object[] { id, name, abi, code, codeType.toString(), createdDate });
    }

    @Override
    public Contract getById(String id) throws APIException {
        Object[] res = contractService.read(
                contractRegistryAddress, this.abi, null,
                "getById",
                new Object[] { id },
                null);

        if (res == null || res.length < 6 || ArrayUtils.contains(res, null)) {
            return null; // extra null checks
        }

        long createdDate = ((BigInteger) res[5]).longValue();
        if (((String) res[0]).contentEquals("0x00") || createdDate == 0) {
            return null; // contract is not [yet] registered
        }

        return new Contract(
                (String) res[0],
                (String) res[1],
                (String) res[2],
                (String) res[3],
                CodeType.valueOf((String) res[4]),
                null,
                createdDate);
    }

    @Override
    public Contract getByName(String name) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() throws APIException {

        Object[] res = contractService.read(
                contractRegistryAddress, this.abi, null,
                "listAddrs", null, null);

        Object[] addrs = (Object[]) res[0];

        List<Contract> contracts = new ArrayList<>();
        for (int i = 0; i < addrs.length; i++) {
            String addr = (String) addrs[i];
            try {
                contracts.add(getById(addr));
            } catch (APIException ex) {
                LOG.warn("error loading contract details for " + addr, ex);
            }
        }

        return contracts;
    }

    @Override
    public List<Contract> listByOwner(String owner) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

}
