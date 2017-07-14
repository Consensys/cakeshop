package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.model.APIResponse;
import static com.jpmorgan.cakeshop.service.WebSocketPushService.GETH_LOG_TOPIC;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LogTailerListener extends TailerListenerAdapter {

    @Autowired(required = false)
    private SimpMessagingTemplate template;

    @Override
    public void handle(String line) {
        if (StringUtils.isNotBlank(line)) {
            template.convertAndSend(GETH_LOG_TOPIC,
                    APIResponse.newSimpleResponse(line));
        }
    }

}
