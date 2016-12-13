package com.foo.bar.contract;

import static com.jpmorgan.cakeshop.client.ContractUtil.*;

import com.jpmorgan.cakeshop.client.ContractProxy;
import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.client.model.TransactionResult;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.model.ContractABI;

import java.math.BigInteger;
import java.util.List;

/**
 * This contract implementation extends from {@link ContractProxy} and directly uses the APIs
 * to read and transaction with the blockchain. This gives more granular control over how calls
 * are made and handled. For example, if data needs to be transformed in some way before being sent
 * to the chain.
 *
 * @author Chetan Sarva
 *
 */
public class SimpleStorageContract extends ContractProxy<SimpleStorageContract> {

  public interface METHODS {
    public static final String storedData = "storedData";
    public static final String set = "set";
    public static final String get = "get";
  }

  public static final String jsonAbi = "[{\"constant\":true,\"inputs\":[],\"name\":\"storedData\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"retVal\",\"type\":\"uint256\"}],\"type\":\"function\"},{\"inputs\":[{\"name\":\"initVal\",\"type\":\"uint256\"}],\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"message\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"newVal\",\"type\":\"uint256\"}],\"name\":\"Change\",\"type\":\"event\"}]";

  public static final ContractABI abi = ContractABI.fromJson(jsonAbi);

  public static SimpleStorageContract at(ContractApi contractApi, String contractAddress) {
      return new SimpleStorageContract(contractApi, contractAddress);
  }

  private SimpleStorageContract(ContractApi contractApi, String contractAddress) {
      super(contractApi, contractAddress);
  }

  @Override
  public ContractABI getABI() {
    return abi;
  }


  public List<Object> storedData() {
    APIResponse<List<Object>, Object> res = contractApi.read(newCall(METHODS.storedData));
    return processOutputArgs(res.getApiData(), abi.getFunction(METHODS.storedData).outputs);
  }

  public TransactionResult set(BigInteger x) {
    Object[] inputs = new Object[]{ x };
    processInputArgs(inputs);
    APIResponse<APIData<TransactionResult>, TransactionResult> res = contractApi.transact(newCall(METHODS.set, inputs));
    return res.getData();
  }

  public List<Object> get() {
    APIResponse<List<Object>, Object> res = contractApi.read(newCall(METHODS.get));
    return processOutputArgs(res.getApiData(), abi.getFunction(METHODS.get).outputs);
  }

}