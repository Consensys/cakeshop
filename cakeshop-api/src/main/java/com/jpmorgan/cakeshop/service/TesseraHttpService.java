package com.jpmorgan.cakeshop.service;

import java.util.Map;

/**
 *
 * @author pavan
 */
public interface TesseraHttpService {

  public String getUpdateCheck(String nodeUrl);

  public String getVersion(String nodeUrl);

  public Map<String, Object> getPartyInfo(String nodeUrl);
}
