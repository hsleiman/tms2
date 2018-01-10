/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.objectbrains.scheduler.annotation.QuartzJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * 
 */
@QuartzJob(name = StopDialerJob.NAME, group = StopDialerJob.GROUP)
public class StopDialerJob extends DialerQuartzJob {

    public static final String NAME = "stop";

    @Override
    protected void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException {
        try {
            dialer.stop();
        } catch (DialerException ex) {
            JobExecutionException jex = new JobExecutionException(ex);
            jex.setRefireImmediately(true);
            throw jex;
        }
    }

}
