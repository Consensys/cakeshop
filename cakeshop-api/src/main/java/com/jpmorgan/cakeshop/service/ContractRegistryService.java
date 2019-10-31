package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import java.util.List;

public interface ContractRegistryService {

    /**
     * Deploy the Contract Registry contract onto the chain.
     *
     * Primarily used during chain initialization.
     *
     * @return
     * @throws APIException
     */
    public void deploy() throws APIException;

    /**
     * Update the location of the ContractRegistry
     *
     * @param addr String
     *
     * @throws APIException
     */
    public void updateRegistryAddress(String addr) throws APIException;

    /**
     * Register a new contract
     *
     * @param id
     * @param name
     * @param abi
     * @param code
     * @param codeType
     * @param createdDate
     * @param privateFor, used to determine whether to register the contract publicly
     *
     * @return {@link TransactionResult} The id of the registration transaction
     * @throws APIException
     */
    public TransactionResult register(String from, String id, String name, String abi, String code,
        CodeType codeType, Long createdDate, String privateFor) throws APIException;

    /**
     * Lookup a contract in the registry by ID (address hash)
     *
     * @param id                Address hash of contract
     *
     * @return
     * @throws APIException
     */
    public Contract getById(String id) throws APIException;

    /**
     * Lookup a contract in the registry by name. If multiple instances are
     * stored, will return the latest version (by timestamp).
     *
     * @param name
     * @return
     * @throws APIException
     */
    public Contract getByName(String name) throws APIException;

    /**
     * List all contracts in the registry
     *
     * @return
     * @throws APIException
     */
    public List<Contract> list() throws APIException;

    /**
     * List all contracts registered by the given address
     *
     * @param owner           Address hash to lookup
     * @return
     * @throws APIException
     */
    public List<Contract> listByOwner(String owner) throws APIException;

    /**
     * Determine whether the registry needs to be deployed
     *
     * @return whether the contract exists
     */
    public boolean contractRegistryExists();

    /**
     * Lookup the address of the contract from application.properties
     *
     * @return the address where the contract registry is supposed to exist
     */
    String getAddress();
}
