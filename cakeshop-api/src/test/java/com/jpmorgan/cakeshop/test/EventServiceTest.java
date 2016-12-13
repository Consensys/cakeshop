package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.model.Event;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.EventService;
import com.jpmorgan.cakeshop.service.TransactionService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class EventServiceTest extends BaseGethRpcTest {

	@Autowired
	private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EventService eventService;

	@Test
	public void testListForBlock() throws IOException, InterruptedException {
        try {
            String addr = createContract();
            TransactionResult tr = contractService.transact(addr, null, "set", new Object[]{ 100 });
            Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);

            List<Event> events = eventService.listForBlock(tx.getBlockNumber().longValue());
            assertNotNull(events);
            assertFalse(events.isEmpty());
            assertEquals(events.size(), 1);

            testEvent(events.get(0));
            testEvent(tx.getLogs().get(0));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EventServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

    private void testEvent(Event event) throws IOException, ClassNotFoundException {
        Object[] data = event.getData();
        assertNotNull(data);
		assertEquals(data.length, 2);
		assertEquals(data[0], "change val");
		assertEquals(data[1], BigInteger.valueOf(100L));

    }

}
