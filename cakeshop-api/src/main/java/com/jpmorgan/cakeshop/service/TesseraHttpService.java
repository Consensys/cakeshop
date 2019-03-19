package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.model.Tessera;

import java.util.Map;

/**
 *
 * @author pavan
 */
public interface TesseraHttpService {

  public String getUpdateCheck(String nodeUrl);

  public String getVersion(String nodeUrl);

  public Tessera getPartyInfo(String nodeUrl);

}
