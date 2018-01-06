/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 *
 * @author connorpetty
 */
public abstract class DialerQuartzJob extends QuartzJobBean {

    public static final String GROUP = "dialer";

    @Autowired
    protected DialerService dialerService;

    protected Long dialerPk;

    public Long getDialerPk() {
        return dialerPk;
    }

    public void setDialerPk(Long dialerPk) {
        this.dialerPk = dialerPk;
    }

    public static JobDataMap buildDataMap(long dialerPk) {
        JobDataMap jobData = new JobDataMap();
        jobData.putAsString("dialerPk", dialerPk);
        return jobData;
    }

    @Override
    protected final void executeInternal(JobExecutionContext context) throws JobExecutionException {
        if (dialerPk == null) {
            JobExecutionException jex = new JobExecutionException("queuePk is null");
            jex.setUnscheduleFiringTrigger(true);
            throw jex;
        }
        Dialer dialer = dialerService.getDialerByPk(dialerPk);
        if (dialer == null) {
            JobExecutionException jex = new JobExecutionException(String.format("No dialer found with dialerPk [%d]", dialerPk));
            jex.setUnscheduleFiringTrigger(true);
            throw jex;
        }
        if (dialer.getState() == Dialer.State.STOPPED) {
            JobExecutionException jex = new JobExecutionException(String.format("Dialer [queuePk=%d,dialerPk=%d] is stopped", dialer.getQueuePk(), dialerPk));
            jex.setUnscheduleFiringTrigger(true);
            throw jex;
        }
        if (dialer.getState() == Dialer.State.COMPLETED) {
            JobExecutionException jex = new JobExecutionException(String.format("Dialer [queuePk=%d,dialerPk=%d] is completed", dialer.getQueuePk(), dialerPk));
            jex.setUnscheduleFiringTrigger(true);
            throw jex;
        }
        executeInternal(context, dialer);
    }

    protected abstract void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException;
}
