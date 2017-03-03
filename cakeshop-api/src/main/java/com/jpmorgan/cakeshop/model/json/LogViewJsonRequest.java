/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model.json;

public class LogViewJsonRequest {

    private String logType, logFileName;
    private Integer numberLines;

    /**
     * @return the logType
     */
    public String getLogType() {
        return logType;
    }

    /**
     * @param logType the logType to set
     */
    public void setLogType(String logType) {
        this.logType = logType;
    }

    /**
     * @return the logFileName
     */
    public String getLogFileName() {
        return logFileName;
    }

    /**
     * @param logFileName the logFileName to set
     */
    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    /**
     * @return the numberLines
     */
    public Integer getNumberLines() {
        return numberLines;
    }

    /**
     * @param numberLines the numberLines to set
     */
    public void setNumberLines(Integer numberLines) {
        this.numberLines = numberLines;
    }

}
