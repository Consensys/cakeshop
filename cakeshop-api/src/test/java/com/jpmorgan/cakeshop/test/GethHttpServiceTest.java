package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.service.BlockService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class GethHttpServiceTest extends BaseGethRpcTest {

    @Autowired
    private GethHttpService geth;

    @Autowired
    private BlockService blockService;

    @Test
    public void testReset() throws APIException {
        System.out.println("Running  GethHttpServiceTest.testReset-------------------------------------");
        assertTrue(geth.isRunning());

        String blockId = blockService.get(null, 1L, null).getId();

        assertTrue(geth.reset());
        assertTrue(geth.isRunning());

        assertNotEquals(blockService.get(null, 1L, null).getId(), blockId);
    }

}
