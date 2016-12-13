package com.jpmorgan.cakeshop.service;

import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 *
 * @author Michael Kazansky
 */
public interface WebSocketAsyncPushService {

    public void pushTransactionAsync(String transactionAddress, SimpMessagingTemplate template, Map <String, Integer> transactions);

}
