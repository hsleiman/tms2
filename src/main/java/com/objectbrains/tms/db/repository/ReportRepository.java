/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.db.repository;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.tms.db.entity.ReportSql;
import com.objectbrains.tms.exception.ReportNotFoundException;
import com.objectbrains.tms.pojo.report.AliasToBeanResultConverter;
import com.objectbrains.tms.pojo.report.AliasToEntityLinkedMapResultTransformer;
import com.objectbrains.tms.pojo.report.DialerRunAgentStats;
import com.objectbrains.tms.pojo.report.DialerRunStats;
import com.objectbrains.tms.pojo.report.DialerStatsReport;
import com.objectbrains.tms.pojo.report.TodaysAgentProductivity;
import com.objectbrains.tms.restfull.pojo.ReportKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.QueryTimeoutException;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author hsleiman
 */
@Repository
@Transactional
public class ReportRepository {

    private static final Logger log = LoggerFactory.getLogger(ReportRepository.class);

    private String todaysAgentProductivityReportSql;
    private String currentDialerServiceStatsSql;
    private String dialerRunStatsSql;
    private String dialerRunAgentStatsSql;
    private String dialerRunStatsForQueueSql;

    private ConversionService conversionService = new DefaultConversionService();

    private AliasToBeanResultConverter dialerStatsReportTransformer = new AliasToBeanResultConverter(DialerStatsReport.class, conversionService);
    private AliasToBeanResultConverter dialerRunStatsTransformer = new AliasToBeanResultConverter(DialerRunStats.class, conversionService);
    private AliasToBeanResultConverter dialerRunAgentStatsTransformer = new AliasToBeanResultConverter(DialerRunAgentStats.class, conversionService);

    @ConfigContext
    private ConfigurationUtility config;

    @PostConstruct
    private void init() throws IOException {
        todaysAgentProductivityReportSql = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/sql/TodaysAgentProductivityReports.sql"));
        currentDialerServiceStatsSql = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/sql/CurrentDialerServiceStatsForQueue.sql"));
        dialerRunStatsSql = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/sql/DialerRunStats.sql"));
        dialerRunStatsForQueueSql = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/sql/DialerRunStatsForQueue.sql"));
        dialerRunAgentStatsSql = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/sql/DialerRunAgentStats.sql"));
    }

    @PersistenceContext
    private EntityManager entityManager;

    public List<ReportKey> getReports() {
        return entityManager.createQuery("select "
                + "new " + ReportKey.class.getName() + "(report.pk, report.name) "
                + "from ReportSql report",
                ReportKey.class).getResultList();
    }

    public ReportSql getReport(Long pk) throws ReportNotFoundException {
        ReportSql report = entityManager.find(ReportSql.class, pk);
        if (report == null) {
            throw new ReportNotFoundException(pk);
        }
        return report;
    }

    public Long createReport(String name, String sql) {
        ReportSql report = new ReportSql();
        report.setName(name);
        report.setSql(sql);
        entityManager.persist(report);
        return report.getPk();
    }

    public void updateReport(Long pk, String sql) throws ReportNotFoundException {
        ReportSql report = getReport(pk);
        report.setSql(sql);
//        ReportSql report = new ReportSql();
//        report.setPk(pk);
//        report.setSql(sql);
//        entityManager.merge(report);
    }

    public void deleteReport(Long pk) throws ReportNotFoundException {
        ReportSql report = getReport(pk);
        entityManager.remove(report);
    }

    public List<Map<String, Object>> runReport(Long pk) throws ReportNotFoundException, QueryTimeoutException {
        ReportSql report = getReport(pk);
        Session session = (Session) entityManager.unwrap(Session.class);
        String sql = report.getSql();
        sql = sql.replace("::", "\\:\\:");
        return (List<Map<String, Object>>) session.createSQLQuery(sql)
                .setResultTransformer(AliasToEntityLinkedMapResultTransformer.INSTANCE)
                .setTimeout(60)//1 minute
                .list();
    }

    @Sync
    public List<TodaysAgentProductivity> getTodaysAgentProductivityReports() {
        String sql = config.getString("todaysAgentProductivityReportSql", todaysAgentProductivityReportSql);
        List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
        List<TodaysAgentProductivity> todaysAgentProductivitys = new ArrayList<>();
        for (Object[] result : results) {
            todaysAgentProductivitys.add(new TodaysAgentProductivity(result));
        }
        return todaysAgentProductivitys;
    }

    @Sync
    @SuppressWarnings("unchecked")
    public DialerStatsReport getCurrentDialerStatsReport(long queuePk) {
        String sql = config.getString("currentDialerServiceStatsSql", currentDialerServiceStatsSql);
        Session session = (Session) entityManager.unwrap(Session.class);
        return (DialerStatsReport) session.createSQLQuery(sql)
                .setResultTransformer(dialerStatsReportTransformer)
                .setLong("queuePk", queuePk)
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<DialerRunStats> getDialerRunStats(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = config.getString("dialerRunStatsSql", dialerRunStatsSql);
        Session session = (Session) entityManager.unwrap(Session.class);
        return (List<DialerRunStats>) session.createSQLQuery(sql)
                .setResultTransformer(dialerRunStatsTransformer)
                .setDate("startTime", startTime.toDate())
                .setDate("endTime", endTime.toDate())
                .list();
    }

    @SuppressWarnings("unchecked")
    public DialerRunStats getLatestDialerRunStats(long queuePk) {
        String sql = config.getString("dialerRunStatsForQueueSql", dialerRunStatsForQueueSql);
        Session session = (Session) entityManager.unwrap(Session.class);
        return (DialerRunStats) session.createSQLQuery(sql)
                .setResultTransformer(dialerRunStatsTransformer)
                .setLong("queuePk", queuePk)
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<DialerRunAgentStats> getDialerRunAgentStats(long queuePk, long runId) {
        String sql = config.getString("dialerRunAgentStatsSql", dialerRunAgentStatsSql);
        Session session = (Session) entityManager.unwrap(Session.class);
        return (List<DialerRunAgentStats>) session.createSQLQuery(sql)
                .setResultTransformer(dialerRunAgentStatsTransformer)
                .setLong("queuePk", queuePk)
                .setLong("statsPk", runId)
                .list();
    }

}
