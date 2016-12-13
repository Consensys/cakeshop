package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.service.ContractRegistryService;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 *
 * @author Michael Kazansky
 */
public class ContractRegistryCacheTest extends BaseGethRpcTest {

	@Autowired
    @Qualifier("cacheManager")
	CacheManager manager;

    @Autowired
    ContractRegistryService contractRegistry;


    @Test
	public void testCache() throws IOException, InterruptedException, APIException  {

		Contract first = contractRegistry.list().get(0);
        assertNotNull(first);

        String addr = first.getAddress();

	    // First invocation returns object returned by the method
	    Contract result = contractRegistry.getById(addr);
	    assertEquals(result.getAddress(), first.getAddress());
        Reporter.log("######### ABI " + result.getContractAbi(), true);
	    Reporter.log("######### " + manager.getCache("contracts").get(addr).get(), true);

	    // Second invocation should return cached value
	    Contract result2 = contractRegistry.getById(addr);
	    assertNotNull(result2.getABI());
	    assertEquals(result2.getAddress(), first.getAddress());
        Reporter.log("######### ABI " + result2.getContractAbi(), true);
        Reporter.log("######### " + manager.getCache("contracts").get(addr).get(), true);

	    // Verify repository method was invoked once
	    assertNotNull(manager.getCache("contracts").get(addr).get());
        Contract cachedContract = (Contract)manager.getCache("contracts").get(addr).get();
        assertEquals(cachedContract.getAddress(), first.getAddress());
        Reporter.log("######### Cached ABI " + cachedContract.getContractAbi(), true);

        //Forced null
        addr = "0x1234567890";

        //first invocation with forced null value. Should be null in the cache
        result = contractRegistry.getById(addr);
        assertNull(result);
        Reporter.log("######### WITH FORCED NULL VALUE " + manager.getCache("contracts").get(addr), true);

        //second invocation with forced null value. Should still be null in the cache
        result = contractRegistry.getById(addr);
        assertNull(result);
        Reporter.log("######### WITH FORCED NULL VALUE " + manager.getCache("contracts").get(addr), true);
        assertNull(manager.getCache("contracts").get(addr));
	}
}
