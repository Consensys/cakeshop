package com.jpmorgan.cakeshop.client.proxy;

import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.model.ContractABI;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ContractProxyBuilder {

    private final ContractApi contractApi;

    public ContractProxyBuilder(ContractApi contractApi) {
        this.contractApi = contractApi;
    }

    /**
     * Build an implementation for the given Contract interface. As this uses reflection, the
     * result should be cached and re-used (thread-safe).
     *
     * @param contractAddress
     * @param target
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> target, String contractAddress, ContractABI abi) {
        InvocationHandler handler = new ContractProxyInvocationHandler(contractApi, target, contractAddress, abi);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ target }, handler);
    }

}
