package com.jpmorgan.cakeshop.service.impl;

import com.google.common.collect.ObjectArrays;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.ContractDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private ContractDAO contractDAO;

    @Autowired
    private GethConfig gethConfig;

    private final ContractABI abi;

    public ContractRegistryServiceImpl() throws IOException {
        this.abi = ContractABI.fromJson(FileUtils.readClasspathFile(REGISTRY_ABI_FILE));
    }

    @Override
    public void deploy() throws APIException {
        try {
            String code = FileUtils.readClasspathFile("contracts/ContractRegistry.sol");
            TransactionResult txr = contractService.create(null, code, CodeType.solidity, null, null, null, null,
                "ContractRegistry.sol", true, "byzantium"); // byzantium for most compatibility
            Transaction tx = transactionService.waitForTx(txr, 200, TimeUnit.MILLISECONDS);
            if(tx.isSuccess()) {
                this.contractRegistryAddress = tx.getContractAddress();
                saveContractRegistryAddress(this.contractRegistryAddress);
            } else {
                throw new APIException("Status code for deployment was 0x0, check the geth logs for EVM errors");
            }

        } catch (IOException | InterruptedException e) {
            throw new APIException("Error deploying ContractRegistry to chain: " + e.getMessage(), e);
        }

    }

    private void saveContractRegistryAddress(String addr) throws APIException {
        try {
            LOG.debug("Storing ContractRegistry address " + addr);
            gethConfig.setContractAddress(addr);
            gethConfig.save();
        } catch (IOException e) {
            LOG.warn("Unable to update application.properties", e);
            throw new APIException("Unable to update env.properties", e);
        }
    }

    @Override
    public void updateRegistryAddress(String addr) throws APIException {
        this.contractRegistryAddress = addr;
        saveContractRegistryAddress(addr);
    }

    @Override
    public TransactionResult register(String from, String id, String name, String abi, String code,
        CodeType codeType, Long createdDate, String privateFor) throws APIException {

        if (name.equalsIgnoreCase("ContractRegistry") || id.equals(contractRegistryAddress)) {
            // Solidity compiler now prefixes contract names with ':'
            // In the future it will be "{filename}:{Contractname}"
            LOG.info("Skipping registration for ContractRegistry");
            return null;
        }

        if (noRegistryAddress()) {
            LOG.warn("Not going to register contract since ContractRegistry address is null");
            return null;
        }

        LOG.info("Registering contract {} with address {}", name, id);

        if (StringUtils.isNotBlank(privateFor)) {
            LOG.info("Registering in private local ContractRegistry");
            Contract contract = new Contract(id, name, abi, code, codeType, null, createdDate,
                privateFor);
            try {
                contractDAO.save(contract, contractRegistryAddress);
            } catch (IOException e) {
                throw new APIException("error saving private contract to database", e);
            }
            return null;
        }

        LOG.info("Registering in public ContractRegistry");
        return contractService.transact(
            contractRegistryAddress, this.abi, from,
            "register",
            new Object[]{id, name, abi, code, codeType.toString(), createdDate});
    }

    @Override
    public Contract getById(String id) throws APIException {
        if (noRegistryAddress()) {
            LOG.debug("Skipping lookup because there is not contract registry yet");
            return null;
        }

        try {
            Contract contract = contractDAO.getById(id);
            if (contract != null) {
                return contract;
            }
        } catch (IOException e) {
            throw new APIException("Error reading private contract from database", e);
        }

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
                createdDate,
                "");
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

        List<String> privateAddresses = contractDAO.listAddresses(contractRegistryAddress);
        Object[] addrs = ObjectArrays.concat(
            (Object[]) res[0],
            privateAddresses.toArray(),
            Object.class);

        List<Contract> contracts = new ArrayList<>();
        for (int i = 0; i < addrs.length; i++) {
            String addr = (String) addrs[i];
            try {
                Contract contract = getById(addr);
                if(StringUtils.isNotBlank(contract.getPrivateFor())) {
                    try {
                        // will not succeed if this node is not in privateFor, mark for front end
                        contractService.get(contract.getAddress());
                    } catch (APIException e) {
                        LOG.info("Contract {} is private, marking as such", contract.getAddress());
                        contract.setPrivateFor("private");
                    }
                }
                contracts.add(contract);
            } catch (APIException ex) {
                LOG.warn("error loading contract details for " + addr, ex);
            }
        }

        contracts.sort(Comparator.comparing(Contract::getCreatedDate));

        return contracts;
    }

    @Override
    public List<Contract> listByOwner(String owner) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contractRegistryExists() {
        // test stored address
        if(noRegistryAddress()) {
            return false;
        }
        LOG.info("Loaded contract registry address " + contractRegistryAddress);
        try {
            contractService.get(contractRegistryAddress);
            return true;
        } catch (APIException e) {
            LOG.warn("Contract registry contract doesn't exist at {}", contractRegistryAddress);
        }
        return false;
    }

    private boolean noRegistryAddress() {
        return StringUtils.isEmpty(contractRegistryAddress);
    }

    @Override
    public String getAddress() {
        return contractRegistryAddress;
    }
}
