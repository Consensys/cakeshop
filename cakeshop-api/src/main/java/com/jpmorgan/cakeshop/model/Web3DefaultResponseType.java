package com.jpmorgan.cakeshop.model;

import java.util.Map;

import com.jpmorgan.cakeshop.error.APIException;
import org.web3j.protocol.core.Response;

import com.jpmorgan.cakeshop.util.CakeshopUtils;

public class Web3DefaultResponseType extends Response<Object>{

	public Map<String, Object> getResponse() throws APIException {
		return CakeshopUtils.processWeb3Response(getResult(), getError());
	}
}
