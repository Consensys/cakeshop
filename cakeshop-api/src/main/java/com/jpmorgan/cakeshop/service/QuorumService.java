package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.QuorumInfo;

public interface QuorumService {

    public boolean isQuorum();

    public QuorumInfo getQuorumInfo() throws APIException;

}
