/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize(using = TransactionDeserializer.class)
public class TransPostJsonResquest {

    private String id;
    private String from;
    private String to;
    private String data;
    private String privateFrom;
    private List<String> privateFor;
    private List<String> ids;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the privateFrom
     */
    public String getPrivateFrom() {
        return privateFrom;
    }

    /**
     * @param privateFrom the privateFrom to set
     */
    public void setPrivateFrom(String privateFrom) {
        this.privateFrom = privateFrom;
    }

    /**
     * @return the privateFor
     */
    public List<String> getPrivateFor() {
        return privateFor;
    }

    /**
     * @param privateFor the privateFor to set
     */
    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

    /**
     * @return the ids
     */
    public List<String> getIds() {
        return ids;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

}
