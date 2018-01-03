/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.scheduler.annotation.QuartzJob;
import com.objectbrains.svc.iws.CorrespondenceServiceIWS;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.svc.iws.TextMessagePojo;
import com.objectbrains.svc.iws.UserData;
import com.objectbrains.tms.utility.HttpClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 *
 * @author hsleiman
 */
@DisallowConcurrentExecution
@QuartzJob(name = TextMsgServicing.NAME)
public class TextMsgServicing extends QuartzJobBean {

    static final String NAME = "sendTextMessages";

    @Autowired
    private CorrespondenceServiceIWS iWS;

    private static final Logger LOG = LoggerFactory.getLogger(TextMsgServicing.class);

    @Bean
    public static Trigger textMessagesTrigger() {
        return TriggerBuilder.newTrigger().forJob(NAME)
                .withIdentity(NAME)
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(30).withMisfireHandlingInstructionIgnoreMisfires())
                .startNow()                        
                .build();
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //log.info("Sending Out Text Messages");
        try {
            List<TextMessagePojo> tList = iWS.getPendingTextMessages(new UserData());
            if(tList.isEmpty()){
                return;
            }
            LOG.info("Sending Out Text Messages: " + tList.size());
            for (TextMessagePojo textMessage : tList) {
                String textMsg = URLEncoder.encode(textMessage.getTextMessage(), "UTF-8");
                Long phone = textMessage.getPhoneNumber();
                String username = "7gspdufc";
                String password = "6FdRrUsk";
                String http = "http://www.smsglobal.com/http-api.php?action=sendsms&user=" + username + "&password=" + password + "&&from=Test&to=1" + phone + "&text=" + textMsg;
                try {
                    HttpClient.sendGetRequestAsText(http);
                } catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }
        } catch (SvcException | UnsupportedEncodingException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
