/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull;

import com.objectbrains.commons.joda.LocalDateTimeAdapter;
import com.objectbrains.svc.iws.BorrowerPhoneData;
import com.objectbrains.svc.iws.DialerQueueLoanDetails;
import com.objectbrains.svc.iws.OutboundDialerQueueRecord;
import com.objectbrains.svc.iws.SvcException;
import com.objectbrains.tms.db.repository.ReportRepository;
import com.objectbrains.tms.hazelcast.entity.DialerStats;
import com.objectbrains.tms.hazelcast.entity.WaitingCall;
import com.objectbrains.tms.pojo.DialerSchedule;
import com.objectbrains.tms.pojo.LoanInfoRecord;
import com.objectbrains.tms.pojo.QueueRunningSatus;
import com.objectbrains.tms.pojo.report.AgentProductivity;
import com.objectbrains.tms.pojo.report.DialerRunAgentStats;
import com.objectbrains.tms.pojo.report.DialerRunStats;
import com.objectbrains.tms.pojo.report.DialerStatsReport;
import com.objectbrains.tms.pojo.report.TodaysAgentProductivity;
import com.objectbrains.tms.service.AgentService;
import com.objectbrains.tms.service.AgentStatsService;
import com.objectbrains.tms.service.ReportService;
import com.objectbrains.tms.service.dialer.CallService;
import com.objectbrains.tms.service.dialer.Dialer;
import com.objectbrains.tms.service.dialer.DialerException;
import com.objectbrains.tms.service.dialer.DialerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author connorpetty
 */
@Path("/dialer")
@Produces(MediaType.APPLICATION_JSON)
public class DialerRest {

    private static final Logger LOG = LoggerFactory.getLogger(DialerRest.class);

    @Autowired
    private DialerService dialerService;
//    @Autowired
//    private TMSService tmsIws;
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentStatsService agentStatsService;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ReportService reportService;

    @Autowired
    private CallService callService;

    @Path("/{queuePk}/start")
    @POST
    public void startQueue(@PathParam("queuePk") Long queuePk) throws SvcException, DialerException {
        dialerService.startQueue(queuePk);
    }

    @Path("/{queuePk}/pause")
    @POST
    public void pauseQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.pauseQueue(queuePk);
    }

    @Path("/{queuePk}/resume")
    @POST
    public void resumeQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.resumeQueue(queuePk);
    }

    @Path("/{queuePk}/stop")
    @POST
    public void stopQueue(@PathParam("queuePk") Long queuePk) throws DialerException {
        dialerService.stopQueue(queuePk);
    }
    
    @Path("/{queuePk}/schedule")
    @GET
    public List<DialerSchedule> getSchedule(@PathParam("queuePk") Long queuePk){
        return dialerService.getDialerSchedule(queuePk);
    }
    
    @Path("/{queuePk}/schedule")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void setSchedule(@PathParam("queuePk") Long queuePk, List<DialerSchedule> schedules) throws DialerException{
        dialerService.setDialerSchedule(queuePk, schedules);
    }

    @Path("/{queuePk}")
    @GET
    public DialerStats getDialerStats(@PathParam("queuePk") long queuePk) {
        Dialer dialer = dialerService.getDialer(queuePk);
        if (dialer != null) {
            return dialer.getDialerStats();
        }
        return new DialerStats();
    }

    @Path("/{queuePk}/agent/productivity")
    @GET
    public List<AgentProductivity> getAgentProductivity(@PathParam("queuePk") long queuePk) {
        return reportService.getCurrentAgentProductivityReports(queuePk);
    }

    @Path("/{queuePk}/stats")
    @GET
    public DialerStatsReport getDialerStatsReport(@PathParam("queuePk") long queuePk) {
        return reportRepository.getCurrentDialerStatsReport(queuePk);
    }
    
    @Path("/{queuePk}/run/stats")
    @GET
    public DialerRunStats getDialerRunStatsReport(@PathParam("queuePk") long queuePk) {
        return reportRepository.getLatestDialerRunStats(queuePk);
    }

    @Path("/stats")
    @GET
    public List<DialerRunStats> getDialerRunStats(
            @QueryParam("startTime") String startTimeStr,
            @QueryParam("endTime") String endTimeStr) throws Exception{
        LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();
        return reportRepository.getDialerRunStats(
                adapter.unmarshal(startTimeStr),
                adapter.unmarshal(endTimeStr));
    }

    @Path("/{queuePk}/run/{runId}/agentStats")
    @GET
    public List<DialerRunAgentStats> getDialerRunAgentStats(
            @PathParam("queuePk") long queuePk,
            @PathParam("runId") long runId) {
        return reportRepository.getDialerRunAgentStats(queuePk, runId);
    }

    @Path("/agent/productivityToday")
    @GET
    public List<TodaysAgentProductivity> getTodayAgentProductivityReports() {
        return reportRepository.getTodaysAgentProductivityReports();
    }

    @GET
    public List<QueueRunningSatus> getAllDialerQueueStatus() {
        List<QueueRunningSatus> list = new ArrayList<>();

        List<Dialer> dialers = dialerService.getDialers();
        for (Dialer dialer : dialers) {
            QueueRunningSatus qrs = new QueueRunningSatus();
            qrs.setQueue(dialer.getRecord().getDqPk());
//            try {
                qrs.setRunning(dialer.isRunning());
                qrs.setDialerState(dialer.getState());
//            } catch (DialerException ex) {
//                qrs.setRunning(true);
//                qrs.setDialerState(null);
//                LOG.error("Error occurred in dialer {}", ex.getQueuePk(), ex);
//            }
            list.add(qrs);
        }
        return list;
    }

//    @Path("/{queuePk}/agents")
//    @GET
//    public List<AgentStatus> getAllAgentStatusInQueue(@PathParam("queuePk") int queueId) throws SvcException {
//        List<AgentStatus> retList = new ArrayList<>();
//        DialerQueueRecord record = tmsIws.getOutboundDialerQueueRecord(queueId);
//        List<Agent> agents = agentService.getAgents(record.getAgentWeightPriorityList(), null, null);
//        Map<Integer, AgentStats> stats = agentStatsService.getAgentStats(agents);
//        for (Agent agent : agents) {
//            retList.add(new AgentStatus(agent, stats.get(agent.getExtension())));
//        }
//        return retList;
//    }
    private LoanInfoRecord createLoanRecord(DialerQueueLoanDetails details, Dialer dialer) {
        List<BorrowerPhoneData> data = details.getBorrowerPhoneData();
        if (data.isEmpty()) {
            return null;
        }
        BorrowerPhoneData borrower = data.get(0);
        LoanInfoRecord loanRecord = new LoanInfoRecord();
        loanRecord.setFirstName(borrower.getFirstName());
        loanRecord.setLastName(borrower.getLastName());
        loanRecord.setLoanPk(details.getLoanPk());
        loanRecord.setCompleted(dialer != null && dialer.isLoanComplete(details.getLoanPk()));
        return loanRecord;
    }

    @Path("/{queuePk}/loans/count")
    @GET
    public int getLoanCount(@PathParam("queuePk") long queuePk) throws SvcException {
        Dialer dialer = dialerService.getDialer(queuePk);
        if (dialer == null) {
            return 0;
        }
        OutboundDialerQueueRecord record = dialer.getRecord();
        int count = 0;
        for (DialerQueueLoanDetails details : record.getLoanDetails()) {
            List<BorrowerPhoneData> data = details.getBorrowerPhoneData();
            if (!data.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Path("/{queuePk}/loans")
    @GET
    public List<LoanInfoRecord> getLoans(@PathParam("queuePk") long queuePk) throws SvcException {
        List<LoanInfoRecord> retList = new ArrayList<>();
        Dialer dialer = dialerService.getDialer(queuePk);
        if (dialer != null) {
            OutboundDialerQueueRecord record = dialer.getRecord();
            for (DialerQueueLoanDetails details : record.getLoanDetails()) {
                LoanInfoRecord loanRecord = createLoanRecord(details, dialer);
                if (loanRecord != null) {
                    retList.add(loanRecord);
                }
            }
        }
        return retList;
    }

    @Path("/{queuePk}/loans/{page}/{size}")
    @GET
    public List<LoanInfoRecord> getLoans(@PathParam("queuePk") long queuePk,
            @PathParam("page") int page, @PathParam("size") int size) throws SvcException {
        List<LoanInfoRecord> retList = new ArrayList<>();
        Dialer dialer = dialerService.getDialer(queuePk);
        if (dialer != null) {
            OutboundDialerQueueRecord record = dialer.getRecord();
            int index = 0;
            for (DialerQueueLoanDetails details : record.getLoanDetails()) {
                if (retList.size() >= size) {
                    break;
                }
                List<BorrowerPhoneData> data = details.getBorrowerPhoneData();
                if (data.isEmpty()) {
                    continue;
                }
                if (index < page * size) {
                    index++;
                    continue;
                }

                BorrowerPhoneData borrower = data.get(0);
                LoanInfoRecord loanRecord = new LoanInfoRecord();
                loanRecord.setFirstName(borrower.getFirstName());
                loanRecord.setLastName(borrower.getLastName());
                loanRecord.setLoanPk(details.getLoanPk());
                loanRecord.setCompleted(dialer.isLoanComplete(details.getLoanPk()));
                retList.add(loanRecord);
            }
        }
        return retList;
    }

    @Path("/{queuePk}/waitingCalls")
    @GET
    public Queue<WaitingCall> getWaitingCalls(@PathParam("queuePk") long queuePk) {
        return callService.getWaitingCalls(queuePk);
    }

}
