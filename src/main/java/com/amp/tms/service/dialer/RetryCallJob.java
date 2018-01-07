/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.objectbrains.scheduler.annotation.QuartzJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Hoang, J, Bishistha
 */
@QuartzJob(name = RetryCallJob.NAME, group = RetryCallJob.GROUP)
public class RetryCallJob extends DialerQuartzJob {

    private static final Logger LOG = LoggerFactory.getLogger(RetryCallJob.class);

    public static final String NAME = "retry-call";

    private Long loanPk;
    private Integer numberIndex;

    @Autowired
    private DialerStatsService statsService;

    public static JobDataMap buildDataMap(long dialerPk, LoanNumber loanNumber) {
        JobDataMap data = DialerQuartzJob.buildDataMap(dialerPk);
        data.putAsString("loanPk", loanNumber.getLoanPk());
        data.putAsString("numberIndex", loanNumber.getNumberIndex());
        return data;
    }

    public void setLoanPk(Long loanPk) {
        this.loanPk = loanPk;
    }

    public void setNumberIndex(Integer numberIndex) {
        this.numberIndex = numberIndex;
    }

    @Override
    protected void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException {
        statsService.decrementScheduledCallCount(dialer.getDialerPk());
        dialer.addReadyCall(new LoanNumber(loanPk, numberIndex));
        try {
            dialer.handleReadyLoans();
        } catch (DialerException ex) {
            LOG.error("Error occurred in dialer: {}", dialer.getQueuePk(), ex);
        }
    }

}
