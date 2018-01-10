/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
@ServerEndpoint("/websocket/websocket/test-endpoint")
public class TestEndpoint {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info(String.format("Websocket Session Openned: %s", session.getId()));
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info(String.format("Websocket Session %s Error because of %s", session.getId() + "/", closeReason.getReasonPhrase()));
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");

    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info(String.format("Websocket Session %s Error because of %s", session.getId() + "/", error.getMessage()));
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
    }


    @OnMessage
    public void handleMessage(String message, Session session) {
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info(message);
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
        log.info("-------------------------");
    }

 
}
