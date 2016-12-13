package com.jpmorgan.cakeshop.client.proxy;

import com.google.common.reflect.AbstractInvocationHandler;
import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.client.proxy.annotation.Read;
import com.jpmorgan.cakeshop.client.proxy.annotation.Transact;
import com.jpmorgan.cakeshop.model.ContractABI;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class ContractProxyInvocationHandler extends AbstractInvocationHandler {

    private final Class<?> target;
    private final ContractApi contractApi;
    private final String contractAddress;
    private final ContractABI abi;

    private final Map<Method, ContractMethodHandler> methodHandlers;

    public ContractProxyInvocationHandler(ContractApi contractApi, Class<?> target, String contractAddress, ContractABI abi) {
        this.contractApi = contractApi;
        this.contractAddress = contractAddress;
        this.target = target;
        this.abi = abi;
        this.methodHandlers = new HashMap<>();

        this.createMethodHandlers();
    }

    private void createMethodHandlers() {
        for (Method method : target.getMethods()) {
            if (method.isAnnotationPresent(Read.class) || method.isAnnotationPresent(Transact.class)) {
                methodHandlers.put(method, ContractMethodHandler.create(contractAddress, contractApi, method, abi));
            }
        }
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        return methodHandlers.get(method).invoke(args);
    }

}