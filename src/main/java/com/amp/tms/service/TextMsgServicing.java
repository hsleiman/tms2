/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.tms.utility.HttpClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
           
                String textMsg = URLEncoder.encode("", "UTF-8");
                Long phone = 22l;
                String username = "7gspdufc";
                String password = "6FdRrUsk";
                String http = "http://www.smsglobal.com/http-api.php?action=sendsms&user=" + username + "&password=" + password + "&&from=Test&to=1" + phone + "&text=" + textMsg;
                try {
                    HttpClient.sendGetRequestAsText(http);
                } catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            
        } catch (UnsupportedEncodingException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
