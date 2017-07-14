/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.service.LogViewService;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.stereotype.Component;

@Component
public class LogViewServiceImpl implements LogViewService {

    private String previousLine = "";

    @Override
    public Deque<String> getLog(String logPath, Integer numberOfLines) throws APIException {
        return getLines(logPath, numberOfLines);
    }

    @Override
    public String getLog(String logPath) throws APIException {

        Deque<String> lines = getLines(logPath, 1);

        if (!lines.isEmpty() && !lines.getFirst().equals(previousLine)) {
            previousLine = lines.getFirst();
            return previousLine;
        } else {
            return StringUtils.EMPTY;
        }
    }

    private Deque<String> getLines(String logPath, Integer numberOfLines) throws APIException {
        try {
            File file = new File(logPath);
            int counter = 0;
            Deque<String> lines = new ArrayDeque<>();
            try (ReversedLinesFileReader fileReader = new ReversedLinesFileReader(file, 4000, Charset.forName("UTF-8"))) {
                String line;

                while ((line = fileReader.readLine()) != null && counter++ < numberOfLines) {
                    lines.addFirst(line);
                }
            }

            return lines;
        } catch (IOException ex) {
            throw new APIException(ex.getCause());
        }
    }

}
