package com.jpmorgan.cakeshop.test.controller;

import com.jpmorgan.cakeshop.controller.TesseraController;
import org.testng.annotations.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TesseraControllerTest extends BaseControllerTest {

  @Autowired
  TesseraController tesseraController;

  public TesseraControllerTest() {
    super();
  }


  @Override
  public Object getController() {
    return tesseraController;
  }

  @Test
  public void testUpcheck() throws Exception{

    mockMvc.perform(get("/api/tessera/upcheck"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("testerdfdf!")));
  }
}
