package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;

import java.util.List;
import java.util.Map;

/**
 * @author Michael Kazansky
 */
public interface ReportingHttpService {

    String getReportingUrl();

    String getReportingUiUrl();

    void registerContract(Contract contract) throws APIException;

    Map<String, Object> executeReportingCall(String funcName, Object... args) throws APIException;

    List<String> getRegisteredAddresses() throws APIException;
}
