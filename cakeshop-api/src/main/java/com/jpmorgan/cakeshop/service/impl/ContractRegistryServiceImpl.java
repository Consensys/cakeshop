package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.ContractInfo;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.repo.ContractRepository;
import com.jpmorgan.cakeshop.repo.TransactionRepository;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContractRegistryServiceImpl implements ContractRegistryService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContractRegistryServiceImpl.class);

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper jsonMapper;

    public ContractRegistryServiceImpl() {
    }

    @Override
    public void register(String from, String id, String name, String abi, String code,
                         CodeType codeType, Long createdDate, String storageLayout, String privateFor) throws APIException {

        LOG.info("Registering contract details for {} with address {}", name, id);

        try {
            Contract contract = new Contract(id, name, abi, code, codeType, null, createdDate,
                storageLayout, privateFor);
            ContractInfo contractInfo = new ContractInfo(id, jsonMapper.writeValueAsString(contract));
            contractRepository.save(contractInfo);
        } catch (Exception e) {
            throw new APIException("error saving contract to database", e);
        }
    }

    @Override
    public Contract getById(String id) throws APIException {
        try {
            ContractInfo contractInfo = contractRepository.findById(id).orElse(null);
            if (contractInfo == null) {
                return null;
            }
            return jsonMapper.readValue(contractInfo.contractJson, Contract.class);
        } catch (Exception e) {
            throw new APIException("Error reading contract from database", e);
        }
    }

    @Override
    public Contract getByName(String name) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Contract> list() throws IOException {

        List<Contract> contracts = new ArrayList<>();
        for (ContractInfo contractInfo : contractRepository.findAll()) {
            Contract contract = jsonMapper.readValue(contractInfo.contractJson, Contract.class);
            if (StringUtils.isNotBlank(contract.getPrivateFor())) {
                try {
                    // will not succeed if this node is not in privateFor, mark for front end
                    contractService.get(contract.getAddress());
                } catch (APIException e) {
                    LOG.info("Contract {} is private, marking as such", contract.getAddress());
                    contract.setPrivateFor("private");
                }
            }
            contracts.add(contract);
        }

        return contracts;
    }

    @Override
    public List<Contract> listByOwner(String owner) throws APIException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void migrateContracts(String contractRegistryAddress) {
        LOG.info("Attempting to migrate contracts from the smart contract registry at {} to the database", contractRegistryAddress);
        try {
            ContractABI abi = ContractABI.fromJson(FileUtils.readClasspathFile("contracts" + File.separator + "ContractRegistry.abi.json"));
            Object[] listAddrsRes = contractService.read(
                contractRegistryAddress, abi, null,
                "listAddrs", null, null);

            Object[] addrs = (Object[]) listAddrsRes[0];


            for (int i = 0; i < addrs.length; i++) {
                String addr = (String) addrs[i];
                if (contractRepository.findById(addr).isPresent()) {
                    LOG.info("Contract at address {} is already in the database, skipping.", addr);
                    break;
                }
                LOG.info("Migrating details for contract at {} to the database", addr);
                try {
                    Object[] res = contractService.read(
                        contractRegistryAddress, abi, null,
                        "getById",
                        new Object[]{addr},
                        null);

                    if (res == null || res.length < 6 || ArrayUtils.contains(res, null)) {
                        throw new APIException("Invalid response when looking up contract at " + addr);
                    }

                    long createdDate = ((BigInteger) res[5]).longValue();

                    String address = (String) res[0];
                    String name = (String) res[1];
                    String abi1 = (String) res[2];
                    String code = (String) res[3];
                    CodeType codeType = CodeType.valueOf((String) res[4]);

                    Transaction creationTransaction = transactionRepository.findByContractAddress(address).orElseThrow();
                    String from = creationTransaction.getFrom();

                    register(from,
                        address,
                        name,
                        abi1,
                        code,
                        codeType,
                        createdDate,
                        // storageLayout didn't exist before, and private contracts were saved in the db
                        null,
                        "");

                } catch (APIException ex) {
                    LOG.warn("error migrating contract details for " + addr, ex);
                }
            }
        } catch (IOException e) {
            LOG.error("Error while migrating contracts from the smart contract registry", e);
        }
    }
}
