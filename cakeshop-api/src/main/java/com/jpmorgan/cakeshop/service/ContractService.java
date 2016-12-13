package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionRequest;
import com.jpmorgan.cakeshop.model.TransactionResult;

import java.util.List;

public interface ContractService {

	/**
	 * List of allowable Code Types during contract create
	 * @author Chetan Sarva
	 *
	 */
	public static enum CodeType {
		solidity,
		binary,
		llvm,
		serpent
	}

	/**
	 * Compile the given source code into EVM binary
	 *
	 * @param code
	 * @param codeType
	 * @param optimize
	 * @return
	 * @throws APIException
	 */
	public List<Contract> compile(String code, CodeType codeType, Boolean optimize) throws APIException;

	/**
	 * Deploy the given contract onto the chain and add it to the Contract Registry
	 *
	 * @param from         From address to register with
	 * @param code
	 * @param codeType
	 * @param args         Optional constructor arguments (if needed)
	 * @param binary       If multiple contracts are specified in the source code, a specific binary to deploy can be passed here (Optional)
	 * @param privateFrom  Base64-encoded public key of the sender (optional)
	 * @param privateFor   List of Base64-encoded public keys of recipients (optional)
	 *
	 * @return
	 * @throws APIException
	 */
	public TransactionResult create(String from, String code, CodeType codeType, Object[] args, String binary, String privateFrom, List<String> privateFor) throws APIException;

	/**
	 * Delete the given contract. Not yet implemented.
	 *
	 * @return
	 * @throws APIException
	 */
	public TransactionResult delete() throws APIException;

	/**
	 * Get information about the contract at the given address.
	 *
	 * @param address
	 * @return
	 * @throws APIException
	 */
	public Contract get(String address) throws APIException;

	/**
	 * List all deployed contracts (using the contract registry).
	 *
	 * @return
	 * @throws APIException
	 */
	public List<Contract> list() throws APIException;

	/**
	 * List all transacrtions for the given contract
	 *
	 * @param contractId              Address hash for the contract
	 *
	 * @return
	 * @throws APIException
	 */
	public List<Transaction> listTransactions(String contractId) throws APIException;

	/**
	 * Migrate a contract to a new address. Not yet implemented.
	 *
	 * @return
	 * @throws APIException
	 */
	public TransactionResult migrate() throws APIException;

	/**
	 * Read contract data using the given method and arguments (does not create
	 * a transaction). Optionally read historical state data by passing a block
	 * number in the past. Will automatically lookup the contract's ABI in the registry.
	 *
	 * @param id                  Contract ID (address hash)
	 * @param from                From address
	 * @param method              Method name
	 * @param args                Optional method arguments
	 * @param blockNumber         Block from which to read state from (Optional, defaults to latest)
	 *
	 * @return {@link Object}     Return value(s) as defined in the method's ABI
	 *
	 * @throws APIException
	 */
	public Object[] read(String id, String from, String method, Object[] args, Object blockNumber) throws APIException;

	/**
	 * Read contract data using the given method and arguments (does not create
	 * a transaction). Optionally read historical state data by passing a block
	 * number in the past.
	 *
	 * @param id                  Contract ID (address hash)
	 * @param abi                 Contract ABI
	 * @param from                From address
	 * @param method              Method name
	 * @param args                Optional method arguments
	 * @param blockNumber         Block from which to read the state from (Optional, defaults to latest)
	 *
	 * @return {@link Object}     Return values as defined in the method's ABI
	 *
	 * @throws APIException
	 */
	public Object[] read(String id, ContractABI abi, String from, String method, Object[] args, Object blockNumber) throws APIException;

	public Object[] read(TransactionRequest request) throws APIException;

	/**
	 * Call a transactional method on the given contract. No state will change
	 * until the transaction is committed into a Block.
	 *
	 * @param id                  Contract ID (address hash)
	 * @param from                From address
	 * @param method              Method name
	 * @param args                Optional method arguments
	 *
	 * @return {@link TransactionResult}    Transaction ID
	 *
	 * @throws APIException
	 */
	public TransactionResult transact(String id, String from, String method, Object[] args) throws APIException;

	/**
	 * Call a transactional method on the given contract. No state will change
	 * until the transaction is committed into a Block.
	 *
	 * @param id                  Contract ID (address hash)
	 * @param abi                 Contract ABI
	 * @param from                From address
	 * @param method              Method name
	 * @param args                Optional method arguments
	 *
	 * @return {@link TransactionResult}    Transaction ID
	 *
	 * @throws APIException
	 */
	public TransactionResult transact(String id, ContractABI abi, String from, String method, Object[] args) throws APIException;

	public TransactionResult transact(TransactionRequest request) throws APIException;

}
