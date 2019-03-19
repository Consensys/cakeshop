package com.jpmorgan.cakeshop.model;

import java.io.Serializable;

public class TesseraPeer implements Serializable {

  private String url;

  private String lastContact;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getLastContact() {
    return lastContact;
  }

  public void setLastContact(String lastContact) {
    this.lastContact = lastContact;
  }
}
