package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class Tessera implements Serializable {

  private List<TesseraPeer> peers ;

  private String url;

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @JsonProperty("peers")
  public List<TesseraPeer> getPeers() {
    return peers;
  }

  public void setPeers(List<TesseraPeer> peers) {
    this.peers = peers;
  }
}
