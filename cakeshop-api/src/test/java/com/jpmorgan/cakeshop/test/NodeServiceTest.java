package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.service.NodeService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class NodeServiceTest extends BaseGethRpcTest {

	@Autowired
	private NodeService nodeService;

	@Test
	public void testGet() throws IOException {
	    Node node = nodeService.get();
	    assertNotNull(node);
	    assertEquals(node.getStatus(), "running");
	}

}
