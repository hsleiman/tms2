/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.tms.db.entity.Chat;
import com.amp.tms.db.entity.WebsocketLog;
import com.amp.tms.utility.JsonMapper;
import com.amp.tms.websocket.message.Function;
import com.amp.tms.websocket.message.inbound.Recieve;
import com.amp.tms.websocket.message.outbound.Send;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.LocalDateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hsleiman
 */
@Repository
public class WebsocketRepository {

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketRepository.class);

    private LinkedBlockingQueue<WebsocketLog> deque = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<Chat> dequeChat = new LinkedBlockingQueue<>();

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JsonMapper jsonMapper;

    public void add(WebsocketLog log) {
        log.setCreateTimestamp(LocalDateTime.now());
        log.setCreateTimestampLong(System.currentTimeMillis());
        if (log.getFunction() == Function.Bi) {
            log.setMessage("Removed for size.");
        }
        deque.offer(log);
    }

    @Bean
    public static Trigger cleanWebSocketLogsTrigger() {
        return TriggerBuilder.newTrigger().forJob("cleanWebSocketLogs")
                .withIdentity("cleanWebSocketLogsTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(0, 30)
                        .inTimeZone(TimeZone.getDefault())
                        .withMisfireHandlingInstructionFireAndProceed())
                .startNow()
                .build();
    }

    @QuartzJob(name = "cleanWebSocketLogs", disallowConcurrentExecution = true)
    @Transactional
    public void cleanWebSocketLogs() {
        LOG.info("Cleaning Old WEBSOCKET From DB..");
        entityManager.createNativeQuery("delete from tms.tms_websocket_log where create_timestamp < CURRENT_TIMESTAMP - INTERVAL '14 days'").executeUpdate();
        LOG.info("Cleaning Old WEBSOCKET From DB [DONE]");
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    @Transactional
    public void schedule() {
        if (deque.isEmpty()) {
            return;
        }
        LOG.info("Flushing WEBSOCKET to DB: {}", deque.size());
        List<WebsocketLog> logs = new ArrayList<>();
        deque.drainTo(logs);
        for (WebsocketLog log : logs) {
            entityManager.persist(log);
        }
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    @Transactional
    public void scheduleChat() {
        if (dequeChat.isEmpty()) {
            return;
        }
        LOG.info("Flushing Chat to DB: {}", deque.size());
        List<Chat> chats = new ArrayList<>();
        dequeChat.drainTo(chats);
        for (Chat chat : chats) {
            entityManager.persist(chat);
        }
    }

    public void logEvent(int ext, String event) {
        WebsocketLog websocketLog = new WebsocketLog();
        websocketLog.setDirection(event);
        websocketLog.setExt(ext);
        websocketLog.setFunction(Function.ECHO);
        add(websocketLog);
    }

    public void logChat(int ext, Chat chat) {
        dequeChat.add(chat);
    }

    public void logMessage(int ext, String message, Recieve recieve) {
        WebsocketLog websocketLog = new WebsocketLog();
        websocketLog.setDirection("received");
        websocketLog.setExt(ext);
        websocketLog.setMessage(message);
        websocketLog.setConfirmCode(recieve.getConfirmCode());
        websocketLog.setFunction(recieve.getFunction());
        websocketLog.setCallUUID(recieve.getCall_uuid());
        websocketLog.setIpAddress(recieve.getIpAddress());
        if (recieve.getPhone() != null) {
            websocketLog.setPhone(recieve.getPhone().getStatus());
        }
        add(websocketLog);
    }

    public void logMessage(int ext, Send send) {
        WebsocketLog websocketLog = new WebsocketLog();
        websocketLog.setDirection("sending");
        websocketLog.setExt(ext);
        websocketLog.setConfirmCode(send.getConfirmCode());
        websocketLog.setFunction(send.getFunction());
        try {
            websocketLog.setMessage(jsonMapper.writeValueAsString(send));
        } catch (JsonProcessingException ex) {
            websocketLog.setMessage(send.toJson());
        }
        add(websocketLog);
    }
}
