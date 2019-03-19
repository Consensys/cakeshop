package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.model.Tessera;
import com.jpmorgan.cakeshop.service.TesseraHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Service
public class TesseraHttpServiceImpl implements TesseraHttpService {

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public String getUpdateCheck(String nodeUrl) {

     ResponseEntity<String> response = restTemplate.exchange(nodeUrl, GET,null,String.class);

    return response.getBody();
  }

  @Override
  public String getVersion(String nodeUrl) {
    return null;
  }

  @Override
  public Tessera getPartyInfo(String nodeUrl) {

    ResponseEntity<Tessera> response =  restTemplate.exchange(nodeUrl, GET,null,Tessera.class);

    return response.getBody();

  }
}
