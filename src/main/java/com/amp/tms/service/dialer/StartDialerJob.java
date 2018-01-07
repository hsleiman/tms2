/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.crm.exception.StiException;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 *
 * @author HS
 */
@QuartzJob(name = StartDialerJob.NAME, group = StartDialerJob.GROUP)
public class StartDialerJob extends QuartzJobBean {

    public static final String GROUP = "schedule-dialer";
    public static final String NAME = "start-dialer";

    private static final Logger LOG = LoggerFactory.getLogger(StartDialerJob.class);

    private static final DateTimeFormatter FORMAT = ISODateTimeFormat.time();
    private static final DateTimeFormatter PARSER = ISODateTimeFormat.timeParser();

    public static JobDataMap buildDataMap(long queuePk, LocalTime endTime) {
        JobDataMap data = new JobDataMap();
        data.putAsString("queuePk", queuePk);
        if (endTime != null) {
            data.put("endTimeStr", endTime.toString(FORMAT));
        }
        return data;
    }

    @Autowired
    private DialerService dialerService;

    protected Long queuePk;
    protected String endTimeStr;

    public Long getQueuePk() {
        return queuePk;
    }

    public void setQueuePk(Long queuePk) {
        this.queuePk = queuePk;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            LocalTime endTime = endTimeStr != null ? PARSER.parseLocalTime(endTimeStr) : null;
            dialerService.startQueue(queuePk, endTime);
        } catch (IllegalArgumentException | StiException | DialerException ex) {
            LOG.error("Error trying to start scheduled dialer {}", queuePk, ex);
        } catch (Exception ex) {
            LOG.error("Error trying to start scheduled dialer {}", queuePk, ex);
        }
    }

}
