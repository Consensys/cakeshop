package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Event;


import java.io.IOException;

import java.util.List;

import org.web3j.protocol.core.methods.response.Log;

public interface EventService {

    public List<Event> listForBlock(Long blockNumber) throws APIException;

    public String serialize(Object obj) throws IOException;
    public Object deserialize(String data) throws IOException, ClassNotFoundException;

	List<Event> processEvents(List<Log> txnEvents) throws APIException;


}
