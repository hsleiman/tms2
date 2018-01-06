/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service.dialer;

import com.objectbrains.scheduler.annotation.QuartzJob;
import com.amp.crm.pojo.DialerQueueAccountDetails;
import com.amp.tms.hazelcast.entity.DialerLoan;
import java.util.Map;
import java.util.Queue;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
@QuartzJob(name = ReadyLoansJob.NAME, group = ReadyLoansJob.GROUP)
@DisallowConcurrentExecution
public class ReadyLoansJob extends DialerQuartzJob {

    private static final Logger LOG = LoggerFactory.getLogger(ReadyLoansJob.class);

    public static final String NAME = "loan-scheduler";

    @Autowired
    private DialerStatsService dialerStatsService;

    private boolean bestTimeEnabled;

    public static JobDataMap buildDataMap(long dialerPk, boolean bestTimeEnabled) {
        JobDataMap data = DialerQuartzJob.buildDataMap(dialerPk);
        data.putAsString("bestTimeEnabled", bestTimeEnabled);
        return data;
    }

    public void setBestTimeEnabled(boolean bestTimeEnabled) {
        this.bestTimeEnabled = bestTimeEnabled;
    }

    @Override
    protected void executeInternal(JobExecutionContext context, Dialer dialer) throws JobExecutionException {
        Queue<DialerQueueAccountDetails> notReadyLoans = dialer.getNotReadyLoans();
        Queue<DialerQueueAccountDetails> readyLoans = dialer.getReadyLoans();
        Map<Long, DialerLoan> dialerLoans = dialer.getLoans();
        LocalTime now = LocalTime.now();
        DateTime nextExecutionTime;
        while (true) {
            DialerQueueAccountDetails details = notReadyLoans.peek();
            if (details == null) {
                //no more loans in the notReadyLoans queue
                nextExecutionTime = null;
                break;
            }
            LocalTime bestTime = details.getBestTimeToCall();
            if (bestTimeEnabled && bestTime != null && now.isBefore(bestTime)) {
                nextExecutionTime = bestTime.toDateTimeToday();
                break;
            }
            long loanPk = details.getAccountPk();
            DialerLoan loan = dialerLoans.get(loanPk);
            loan.setState(DialerLoan.State.READY);
            dialerLoans.put(loanPk, loan);
            dialerStatsService.updateStateCount(getDialerPk(), DialerLoan.State.NOT_READY, DialerLoan.State.READY);
            readyLoans.offer(notReadyLoans.poll());
        }
        try {
            if (nextExecutionTime != null && nextExecutionTime.isBefore(dialer.getEndTime())) {
                Trigger oldTrigger = context.getTrigger();
                TriggerKey key = oldTrigger.getKey();

                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity(key)
                        .forJob(context.getJobDetail())
                        .usingJobData(oldTrigger.getJobDataMap())
                        .startAt(nextExecutionTime.toDate())
                        .endAt(dialer.getEndTime().toDate())
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withMisfireHandlingInstructionFireNow())
                        .build();
                context.getScheduler().rescheduleJob(key, trigger);
            } else {
                while (!notReadyLoans.isEmpty()) {
                    long loanPk = notReadyLoans.poll().getAccountPk();
                    DialerLoan loan = dialerLoans.get(loanPk);
                    loan.setState(DialerLoan.State.NEVER_READY);
                    dialerLoans.put(loanPk, loan);
                    dialerStatsService.updateStateCount(getDialerPk(), DialerLoan.State.NOT_READY, DialerLoan.State.NEVER_READY);
                }
            }
            dialer.handleReadyLoans();
        } catch (DialerException | SchedulerException ex) {
            JobExecutionException jex = new JobExecutionException(ex);
            jex.setRefireImmediately(true);
            throw jex;
        }
    }

}
