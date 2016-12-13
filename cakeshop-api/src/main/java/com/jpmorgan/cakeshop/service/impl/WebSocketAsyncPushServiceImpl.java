package com.jpmorgan.cakeshop.service.impl;

import static com.jpmorgan.cakeshop.service.WebSocketPushService.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.Transaction.Status;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WebSocketAsyncPushService;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michael Kazansky
 */
@Component
public class WebSocketAsyncPushServiceImpl implements WebSocketAsyncPushService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebSocketAsyncPushServiceImpl.class);

    @Autowired
    private TransactionService transactionService;

    @Override
    @Async
    public void pushTransactionAsync(final String transactionAddress, final SimpMessagingTemplate template,
            Map<String, Integer> transactions) {

        try {
            Transaction transaction = transactionService.get(transactionAddress);

            if (transaction == null || StringUtils.isBlank(transaction.getId())) {
                APIResponse apiResponse = new APIResponse();
                APIError err = new APIError();
                err.setStatus("404");
                err.setTitle("Transaction not found");
                apiResponse.addError(err);
                if (null != transactions && !transactions.isEmpty()) {
                    transactions.remove(transactionAddress);
                }
                template.convertAndSend(TRANSACTION_TOPIC + transactionAddress, apiResponse);

            } else if (transaction.getStatus() == Status.committed) {
                APIResponse apiResponse = new APIResponse();
                apiResponse.setData(transaction.toAPIData());
                if (null != transactions && !transactions.isEmpty()) {
                    transactions.remove(transactionAddress);
                }
                template.convertAndSend(TRANSACTION_TOPIC + transactionAddress, apiResponse);
            }

        } catch (APIException ex) {
            LOG.error(ex.getMessage());
        }
    }

}
