package com.jpmorgan.cakeshop.test;


import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.TesseraHttpService;
import com.jpmorgan.cakeshop.test.config.TestAppConfig;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.Test;



public class TesseraHttpServiceTest  extends BaseGethRpcTest{

  static {
    System.setProperty("spring.profiles.active", "test");
    System.setProperty("cakeshop.database.vendor", "hsqldb");
  }


  @Autowired
  TesseraHttpService tesseraHttpService;

  @Autowired
  GethHttpService gethHttpService;


  @Test
  public void



  testUpCheckTesseraNode() throws APIException {

    String response =  tesseraHttpService.getUpdateCheck("http://localhost:9006/upcheck");
    //assertTrue("I'm up!".equals(response));
  }


}
