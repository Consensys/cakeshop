package com.jpmorgan.cakeshop.client;

import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.client.model.req.ContractMethodCallCommand;
import com.jpmorgan.cakeshop.model.ContractABI;

public abstract class ContractProxy<T extends ContractProxy<T>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContractProxy.class);

    protected ContractApi contractApi;

    protected String contractAddress;

    protected ContractProxy(ContractApi contractApi, String contractAddress) {
        this.contractApi = contractApi;
        this.contractAddress = contractAddress;
    }

    public abstract ContractABI getABI();

    public ContractMethodCallCommand newCall(String method, Object... args) {
        return new ContractMethodCallCommand().address(contractAddress).method(method).args(args);
    }

    public T contractApi(ContractApi contractApi) {
        this.contractApi = contractApi;
        return (T) this;
    }

    public ContractApi getContractApi() {
        return contractApi;
    }

    public void setContractApi(ContractApi contractApi) {
        this.contractApi = contractApi;
    }

    public T contractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
        return (T) this;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

}
