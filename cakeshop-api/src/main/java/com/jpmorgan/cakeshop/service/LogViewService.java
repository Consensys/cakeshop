package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import java.util.Deque;

public interface LogViewService {

    /**
     *
     * @param logPath
     * @param numberOfLines
     * @return
     * @throws com.jpmorgan.cakeshop.error.APIException
     */
    public Deque<String> getLog(String logPath, Integer numberOfLines) throws APIException;

    public String getLog(String logPath) throws APIException;

}
