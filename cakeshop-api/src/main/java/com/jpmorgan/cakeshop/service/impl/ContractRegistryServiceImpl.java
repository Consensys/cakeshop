package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.model.ContractInfo;
import com.jpmorgan.cakeshop.repo.ContractRepository;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private ObjectMapper jsonMapper;

    public ContractRegistryServiceImpl() {
    }

    @Override
    public void register(String from, String id, String name, String abi, String code,
                         CodeType codeType, String storageLayout, String privateFor) throws APIException {

        LOG.info("Registering contract details for {} with address {}", name, id);

        try {
            ContractInfo contractInfo = contractRepository.findById(id).orElseThrow();
            Contract contract = new Contract(id, name, abi, code, codeType, null, contractInfo.createdDate,
                storageLayout, privateFor);
            contractInfo.name = name;
            contractInfo.contractJson = jsonMapper.writeValueAsString(contract);
            LOG.info("Updating existing contract with id {}", contractInfo.address);
            contractRepository.save(contractInfo);
        } catch (Exception e) {
            throw new APIException("error saving contract to database", e);
        }
    }

    @Override
    public Contract getById(String id) throws APIException {
        try {
            ContractInfo contractInfo = contractRepository.findById(id).orElse(null);
            if(contractInfo == null) {
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
        for (ContractInfo contractInfo : contractRepository.findAllByNameIsNotNullOrderByCreatedDateDesc()) {
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
}
