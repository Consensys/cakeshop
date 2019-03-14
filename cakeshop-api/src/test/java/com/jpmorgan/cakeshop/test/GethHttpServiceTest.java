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

    @Test(enabled = false)
    public void testReset() throws APIException {
        System.out.println("Running  GethHttpServiceTest.testReset-------------------------------------");
        assertTrue(geth.isRunning());

        String blockId = blockService.get(null, 1L, null).getId();
        List<String> additionalParams = new ArrayList<>();
//        additionalParams.add("--blockmakeraccount");
//        additionalParams.add("0xca843569e3427144cead5e4d5999a3d0ccf92b8e");
//        additionalParams.add("--blockmakerpassword");
//        additionalParams.add("");
//        additionalParams.add("--minblocktime");
//        additionalParams.add("2");
//        additionalParams.add("--maxblocktime");
//        additionalParams.add("5");
//        additionalParams.add("--voteaccount");
//        additionalParams.add("0x0fbdc686b912d7722dc86510934589e0aaf3b55a");
//        additionalParams.add("--votepassword");
//        additionalParams.add("");

        assertTrue(geth.reset(additionalParams.toArray(additionalParams.toArray(new String[additionalParams.size()]))));
        assertTrue(geth.isRunning());

        assertNotEquals(blockService.get(null, 1L, null).getId(), blockId);
    }

}
