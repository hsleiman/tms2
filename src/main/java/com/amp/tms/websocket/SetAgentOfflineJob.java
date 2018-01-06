/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket;

import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.tms.enumerated.SetAgentState;
import com.amp.tms.service.TMSAgentService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Deprecated
@QuartzJob(name = SetAgentOfflineJob.NAME, durable = false)
public class SetAgentOfflineJob extends QuartzJobBean {

    public static final String NAME = "SetAgentOffline";

    @Autowired
    private TMSAgentService agentService;

    private Integer ext;

    public Integer getExt() {
        return ext;
    }

    public void setExt(Integer ext) {
        this.ext = ext;
    }

    public static JobDataMap buildDataMap(Integer ext) {
        JobDataMap jobData = new JobDataMap();
        jobData.putAsString("ext", ext);
        return jobData;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        agentService.setAgentState(ext, SetAgentState.LOGOFF);
    }

}
