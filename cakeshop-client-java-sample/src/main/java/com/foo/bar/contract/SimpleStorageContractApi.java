package com.foo.bar.contract;

import com.jpmorgan.cakeshop.client.model.TransactionResult;
import com.jpmorgan.cakeshop.client.proxy.ContractProxyBuilder;
import com.jpmorgan.cakeshop.client.proxy.annotation.Read;
import com.jpmorgan.cakeshop.client.proxy.annotation.Transact;

import java.util.List;

/**
 * This Contract implementation uses the {@link ContractProxyBuilder} to dynamically build
 * an implementation at runtime. The returned implementation instance is fully thread-safe and
 * should be cached and reused.
 *
 * @author Chetan Sarva
 *
 */
public interface SimpleStorageContractApi {

    @Read
    public List<Object> get();

    @Transact
    public TransactionResult set(int val);

}
