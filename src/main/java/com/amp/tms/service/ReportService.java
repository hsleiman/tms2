/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.hazelcast.core.IAtomicReference;
import com.hazelcast.core.IFunction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;
import com.objectbrains.ams.iws.User;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.amp.crm.pojo.DialerQueueRecord;
import com.amp.tms.db.repository.AgentCallHistoryRepository;
import com.amp.tms.db.repository.ReportRepository;
import com.amp.tms.hazelcast.Configs;
import com.amp.tms.hazelcast.entity.AgentStats;
import com.amp.tms.pojo.report.AgentProductivity;
import com.amp.tms.pojo.report.TodaysAgentProductivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class ReportService {

    @Autowired
    private AgentStatsService statsService;

    @Autowired
    private DialerQueueRecordService recordService;

    @Autowired
    private AmsService amsService;

    @Autowired
    private HazelcastService hazelcastService;

    @Autowired
    private AgentCallHistoryRepository agentCallHistoryRepository;

    private IAtomicReference<TodaysReportCache> todaysAgentReportsRef;

    @PostConstruct
    private void initialize() {
        todaysAgentReportsRef = hazelcastService.getAtomicReference(Configs.TODAYS_REPORT_CACHE_REF);
    }

    public List<TodaysAgentProductivity> getTodaysAgentProductivityReports() {
        return todaysAgentReportsRef.alterAndGet(new TodaysReportCacheFunction()).reports;
    }

    public List<AgentProductivity> getCurrentAgentProductivityReports(long queuePk) {
        DialerQueueRecord record = recordService.getDialerQueueRecord(queuePk);
        List<User> users = amsService.getUsers(record.getAgentWeightPriorityList());
        return getCurrentAgentProductivityReports(queuePk, users);
    }

    public List<AgentProductivity> getCurrentAgentProductivityReports(long queuePk, List<User> users) {
        Set<Integer> extensions = Utils.getExtensions(users);
        Map<Integer, AgentStats> statsMap = statsService.getAgentStats(extensions);
        LocalDateTime now = LocalDateTime.now();
        List<AgentProductivity> reports = new ArrayList<>();
        for (User user : users) {
            AgentProductivity report = new AgentProductivity();
            report.setUserName(user.getUserName());
            report.setAgentFirstName(user.getFirstName());
            report.setAgentLastName(user.getLastName());

            AgentStats stats = statsMap.get(user.getExtension());
            if (stats != null) {
                report.setStatsReport(stats.getReport(now));
                report.setCallsReport(agentCallHistoryRepository.getCallHistorySummary(queuePk, user.getExtension(), stats.getStartTime(), now));
            }
            reports.add(report);
        }
        return reports;
    }

//    public List<AgentProductivity> getAllCurrentAgentProductivityReports() {
//        return getCurrentAgentProductivityReports(amsService.getAllUsers());
//    }
    public static class TodaysReportCache implements DataSerializable {

        private LocalDateTime expireTime;

        private List<TodaysAgentProductivity> reports;

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
            out.writeObject(expireTime);
            out.writeInt(reports.size());
            for (TodaysAgentProductivity report : reports) {
                out.writeObject(report);
            }
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
            expireTime = in.readObject();
            int size = in.readInt();
            reports = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                reports.add(in.<TodaysAgentProductivity>readObject());
            }
        }

    }

    @SpringAware
    public static class TodaysReportCacheFunction implements
            IFunction<TodaysReportCache, TodaysReportCache>, DataSerializable {

        @Autowired
        private ReportRepository reportRepository;

        @Override
        public TodaysReportCache apply(TodaysReportCache cache) {
            if (cache == null || LocalDateTime.now().isAfter(cache.expireTime)) {
                cache = new TodaysReportCache();
                cache.reports = reportRepository.getTodaysAgentProductivityReports();
                cache.expireTime = LocalDateTime.now().plusSeconds(5);
            }
            return cache;
        }

        @Override
        public void writeData(ObjectDataOutput out) throws IOException {
        }

        @Override
        public void readData(ObjectDataInput in) throws IOException {
        }

    }

}
