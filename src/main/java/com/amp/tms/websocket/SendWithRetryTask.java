/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.amp.tms.utility.JsonMapper;
import com.amp.tms.websocket.message.outbound.Send;
import java.io.IOException;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * 
 */
@SpringAware
class SendWithRetryTask implements Runnable, DataSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(SendWithRetryTask.class);

    private int ext;
    private Send send;

    @Autowired
    private Websocket websocket;

    @Autowired
    private JsonMapper jsonMapper;

    private SendWithRetryTask() {
    }

    public SendWithRetryTask(int ext, Send send) {
        this.ext = ext;
        this.send = send;
    }

    @Override
    public void run() {
        Session[] sessions = websocket.getSessions(ext);
        if (sessions != null) {
            for (Session session : sessions) {
                websocket.sendWithRetry(ext, session, send);
            }
        } else {
            try {
                LOG.warn("Websocket was unable to find ext [{}] in sessions, failed to send message: {}", ext, jsonMapper.toPrettyJson(send));
            } catch (JsonProcessingException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(ext);
        out.writeObject(send);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        ext = in.readInt();
        send = in.readObject();
    }

}
