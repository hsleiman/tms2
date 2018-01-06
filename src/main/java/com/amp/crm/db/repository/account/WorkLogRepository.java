/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.account;

import com.objectbrains.config.CollectionLogConfigs;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.amp.crm.constants.WorkLogConstants;
import com.amp.crm.constants.WorkLogTypes;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.WorkQueue;
import com.amp.crm.db.entity.disposition.CallDispositionCode;
import com.amp.crm.db.entity.log.Work100Log;
import com.amp.crm.db.entity.log.Work200Log;
import com.amp.crm.db.entity.log.Work300Log;
import com.amp.crm.db.entity.log.Work400Log;
import com.amp.crm.db.entity.log.Work500Log;
import com.amp.crm.db.entity.log.WorkCallLog;
import com.amp.crm.db.entity.log.WorkMainLog;
import com.amp.crm.db.entity.log.WorkManagementReviewLog;
import com.amp.crm.db.entity.log.WorkReviewLog;
import com.amp.crm.db.entity.log.DataChangeLog;
import com.amp.crm.db.hibernate.ThreadAttributes;
import com.amp.crm.embeddable.WorkLogData;
import com.amp.crm.exception.ObjectNotFoundException;
import com.amp.crm.exception.TooManyObjectFoundException;
import com.amp.crm.pojo.WorkLogPojo;
import com.amp.crm.pojo.UserData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author David
 */
@Repository
public class WorkLogRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @ConfigContext
    private CollectionLogConfigs worklLogCofigs;

    public static final Logger LOG = LoggerFactory.getLogger(WorkLogRepository.class);

    public void markAccountReviewed(Account account) {
        WorkReviewLog log = new WorkReviewLog();
        log.setLogType(WorkLogTypes.WORK_LOG_REVIEWED);
        log.setDescription("Review log.");
        log.setAgentUsername(getAgentUsername());
        log.setAccountPk(account.getPk());
        entityManager.persist(log);
    }

    /*public void createManagementReviewLog(Account account, String description, LocalDate expireDate, boolean priority) {
     WorkManagementReviewLog log = new WorkManagementReviewLog();
     log.setLogType(WorkLogTypes.WORK_LOG_MANAGMENT_REVIEWED);
     log.setAccountPk(account.getPk());
     log.setAgentUsername(getAgentUsername());
     log.setDescription(description);
     log.setPriority(priority);
     log.setExpireDate(expireDate);
     log.setCreateTimestamp(LocalDateTime.now());
     entityManager.persist(log);
     }*/
    public WorkMainLog createWorkLog(Account account, String description, int logType, LocalDate expireDate, boolean priority, String callUUID) {

        WorkMainLog log = null;
        if (isBetween(logType, 100, 199)) {
            log = new Work100Log();
        } else if (isBetween(logType, 200, 299)) {
            log = new Work200Log();
        } else if (isBetween(logType, 300, 399)) {
            log = new Work300Log();
        } else if (isBetween(logType, 400, 499)) {
            log = new Work400Log();
        } else if (isBetween(logType, 500, 599)) {
            log = new Work500Log();
        } else if (WorkLogTypes.isManagementReviewLog(logType)) {
            log = new WorkManagementReviewLog();
        } //#176
        else if (WorkLogTypes.isReviewLog(logType)) {
            log = new WorkReviewLog();
        } else {
            log = new WorkMainLog();
        }
        if (logType == WorkLogTypes.WORK_LOG_DIALER_LEFT_MESSAGE) {
            account.getAccountData().setDialerLeftMessageTime(new LocalDateTime());
            description = "Dialer Left Message at " + new LocalDateTime();
        }
        log.setAccountPk(account.getPk());
        log.setAgentUsername(getAgentUsername());
        log.setDescription(description);
        log.setPriority(priority);
        log.setExpireDate(expireDate);
        log.setLogType(logType);
        log.setCallUUID(callUUID);

        if (logType == WorkLogTypes.WORK_LOG_CONTACT_MADE_FOLLOW_UP_PTP || logType == WorkLogTypes.WORK_LOG_INBOUND_CONTACT_MADE
                || logType == WorkLogTypes.WORK_LOG_OUTBOUND_CONTACT_MADE) {
            account.getAccountData().setMyQueuelastContactTimestamp(LocalDateTime.now());
            account.getAccountData().setLastContactTimestamp(LocalDateTime.now());
            account.getAccountData().setLastWorkedDateTime(LocalDateTime.now());
        }
        if (logType == WorkLogTypes.WORK_LOG_LEFT_MESSAGE) {
            //account.setLastContactTimestamp(LocalDateTime.now());
            account.getAccountData().setLastLeftMessageTime(new LocalDateTime());
            account.getAccountData().setLastWorkedDateTime(LocalDateTime.now());
        }

        if (logType == WorkLogTypes.WORK_LOG_DATA_CHANGE
                || logType == WorkLogTypes.WORK_LOG_ACH_CHANGE
                || logType == WorkLogTypes.WORK_LOG_ADDRESS_CHANGE
                || logType == WorkLogTypes.WORK_LOG_PHONE_CHANGE
                || logType == WorkLogTypes.WORK_LOG_EMAIL_CHANGE
                || logType == WorkLogTypes.WORK_LOG_STATUS_CHANGE
                || logType == WorkLogTypes.WORK_LOG_INFORMATION
                || logType == WorkLogTypes.WORK_LOG_NO_MESSAGE
                || logType == WorkLogTypes.WORK_LOG_CALL_CANCELED
                || logType == WorkLogTypes.WORK_LOG_CALL_BUSY
                || logType == WorkLogTypes.WORK_LOG_CALL_DISCONNECTED
                || logType == WorkLogTypes.WORK_LOG_MESSAGE_RECEIVED
                || logType == WorkLogTypes.WORK_LOG_SKIP_TRACE_REQUESTED
                || logType == WorkLogTypes.WORK_LOG_SKIP_TRACE_COMPLETE
                || logType == WorkLogTypes.WORK_LOG_FINACIAL_STATEMENT
                || logType == WorkLogTypes.WORK_LOG_LOAN_PAYMENT_PLUS
                || logType == WorkLogTypes.WORK_LOG_EMPLOYMENT_VERIFICATION
                || logType == WorkLogTypes.WORK_LOG_ACH_BY_PHONE_PAYMENT_REQ
                || logType == WorkLogTypes.WORK_LOG_ACH_BY_PHONE_PENDING_PAYMENT
                || logType == WorkLogTypes.WORK_LOG_THIRD_PARTY_CONTACT) {
            account.getAccountData().setLastWorkedDateTime(LocalDateTime.now());
        }
        if (!account.getWorkQueues().isEmpty()) {
            for (WorkQueue workQueue : account.getWorkQueues()) {

                if (workQueue.getPrimaryAgent() != null && getAgentUsername().equals(workQueue.getPrimaryAgent().getUserName())) {
                    account.getAccountData().setLastWorkedDateTimeByQueueOwner(LocalDateTime.now());
                }
            }
        }
        entityManager.persist(log);
        return log;
    }

    public WorkMainLog createWorkLog(Account account, String description, int logType, LocalDate expireDate, boolean priority) {
        return WorkLogRepository.this.createWorkLog(account, description, logType, expireDate, priority, null);
    }

    //@Async
    //@Transactional
    public void createMegasysWorkLog(long accountPk, String description, int logType, LocalDate expireDate, boolean priority, LocalDateTime creationTime, String username) {
        Account account = accountRepository.findAccountByPk(accountPk);
        Work100Log log = new Work100Log();
        log.setAccountPk(account.getPk());
        log.setAgentUsername(getAgentUsername());
        log.setDescription(description);
        log.setPriority(priority);
        log.setExpireDate(expireDate);
        log.setLogType(logType);
        log.setAgentUsername(username);

        entityManager.persist(log);
        LOG.info("After persist {}", log);
    }

    public void createWorkLogonImport(long accountPk, String description, int logType, LocalDate expireDate, boolean priority, boolean isSkipTraceLog) {
        WorkMainLog log = null;
        if (isBetween(logType, 100, 199)) {
            log = new Work100Log();
        } else if (isBetween(logType, 200, 299)) {
            log = new Work200Log();
        } else if (isBetween(logType, 300, 399)) {
            log = new Work300Log();
        } else if (isBetween(logType, 400, 499)) {
            log = new Work400Log();
        } else if (isBetween(logType, 500, 599) && logType != 500 && logType != 510) {
            log = new Work500Log();
        } else if (logType == 510) {
            log = new WorkManagementReviewLog();
        } else if (logType == 500) {
            log = new WorkReviewLog();
        } else {
            log = new WorkMainLog();
        }
        log.setLogType(logType);
        log.setAccountPk(accountPk);
        log.setAgentUsername(getAgentUsername());
        log.setDescription(description);
        log.setPriority(priority);
        log.setExpireDate(expireDate);
        entityManager.persist(log);
    }

    public void clearWorkLog(WorkMainLog log) {
        log.setClear(true);
        log.setClearedBy(getAgentUsername());
        log.setClearedOn(LocalDateTime.now());
    }

    public void markWorkLogAsReviewed(WorkMainLog log) {
        log.setReviewed(true);
        log.setReviewedBy(getAgentUsername());
        log.setReviewedOn(LocalDateTime.now());
    }

    public List<WorkMainLog> getAllWorkLogsForAccount(Account account) {
        return getAllPriorityWorkMainLogsForAccount(account, 0, null, null, true);
    }

    public List<WorkMainLog> getAllWorkLogsForAccount(Account account, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk AND s.deleted = false AND s.logType not in :logType";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        List<WorkMainLog> res;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkMainLog> getAllPriorityWorkMainLogsForAccount(Account account, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk AND s.deleted = false AND s.priority = true";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        List<WorkMainLog> res;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkMainLog> getAllWorkLogsForAccountSortedByPriority(Account account, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {

        List<WorkMainLog> managementReviewLogs = getManagementReviewLogs(account, null, maxCount, startDate, endDate, includeHiddenLogs);

        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk AND s.deleted = false AND s.logType !=51";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate AND s.priority = true");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }

        sqlQuery = sqlQuery.concat(" ORDER BY s.priority DESC, s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        List<WorkMainLog> res;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }

        Set<WorkMainLog> finalWorkLogs = new LinkedHashSet<>(managementReviewLogs);
        finalWorkLogs.addAll(res);

        return new ArrayList<>(finalWorkLogs);

    }

    public WorkMainLog getWorkLogByPk(long pk) {
        TypedQuery<WorkMainLog> query = entityManager.createQuery("SELECT s FROM WorkMainLog s WHERE s.pk = :pk AND s.deleted = false", WorkMainLog.class);
        query.setParameter("pk", pk);
        List<WorkMainLog> res = query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            throw new ObjectNotFoundException("Work Log not found with given PK" + pk);
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many Logs with given Pk " + pk);
        }
        return res.get(0);
    }

    public List<WorkMainLog> getFilteredWorkLogForAccount(Account account, List logType, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk AND s.logType in :logType AND s.deleted = false";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        query.setParameter("logType", logType);
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        List<WorkMainLog> res;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkLogPojo> convertMainLogToPojo(List<WorkMainLog> mainLog) {
        List<WorkLogPojo> logPojoList = new ArrayList<>();
        for (WorkMainLog log : mainLog) {
            WorkLogPojo pojo = new WorkLogPojo();
            WorkLogData logData = new WorkLogData();
            logData.setLogType(log.getLogType());
            logData.setLogTypeDesc(WorkLogTypes.getLogTypeDesc(log.getLogType()));
            pojo.setWorkLogData(logData);
            pojo.setAccountPk(log.getAccountPk());
            pojo.setLogNote(log.getDescription());
            pojo.setPriority(log.isPriority());
            pojo.setCallUUID(log.getCallUUID());
            pojo.setClear(log.isClear());
            pojo.setClearedBy(log.getClearedBy());
            pojo.setReviewed(log.isReviewed());
            pojo.setUserName(log.getAgentUsername());
            pojo.setWorkLogPk(log.getPk());
            logPojoList.add(pojo);
        }
        return logPojoList;
    }

    public List<WorkLogPojo> getFilteredWorkLogForAccountBasedOnPriority(Account account, List logType, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        List<WorkMainLog> nonPriorityMainLogs = null;
        List<WorkMainLog> priorityMainLogs = new ArrayList<>();
        List<WorkMainLog> managementReviewLogs = null;
        List<WorkLogPojo> priorityDCLogs = new ArrayList<>();
        List<WorkLogPojo> nonPriorityDcLogs = null;
        List<WorkLogPojo> allNonPriorityLogs = new ArrayList<>();
        List<WorkLogPojo> allPriorityLogs = new ArrayList<>();
        List<WorkLogPojo> mgmtPojo = new ArrayList<>();
        List<WorkLogPojo> finalLogsPojo = new ArrayList<>();
        List<WorkLogPojo> finalNonPrioritySortedByTimeStamp = null;

        if (logType.contains(WorkLogTypes.WORK_LOG_MANAGMENT_REVIEW)) {
            managementReviewLogs = getManagementReviewLogs(account, logType, maxCount, startDate, endDate, includeHiddenLogs);
        }

        int additionalLogs = maxCount;

        if (managementReviewLogs != null && !managementReviewLogs.isEmpty()) {
            mgmtPojo = convertMainLogToPojo(managementReviewLogs);
            additionalLogs = additionalLogs - managementReviewLogs.size();
        }
        finalLogsPojo.addAll(mgmtPojo);

        if (additionalLogs > 0) {
            priorityMainLogs = getPriorityWorkLogsForAccount(account, logType, additionalLogs, startDate, endDate, includeHiddenLogs);
        }
        int priorityMainLogsSize = 0, priorityDCLogsSize = 0;
        if (priorityMainLogs != null && !priorityMainLogs.isEmpty()) {
            allPriorityLogs = convertMainLogToPojo(priorityMainLogs);
            //additionalLogs = additionalLogs - allPriorityLogs.size();
            priorityMainLogsSize = priorityMainLogs.size();
        }

        if (logType.contains(WorkLogTypes.WORK_LOG_DATA_CHANGE)) {
            priorityDCLogs = getAllPriorityDataChangeLogs(account, additionalLogs, startDate, endDate);
        }

        if (priorityDCLogs != null && !priorityDCLogs.isEmpty()) {
            allPriorityLogs.addAll(priorityDCLogs);
            priorityDCLogsSize = priorityDCLogs.size();
        }

        finalLogsPojo.addAll(getLogListSortedByTimeStamp(allPriorityLogs, additionalLogs));
        additionalLogs = additionalLogs - (priorityMainLogsSize + priorityDCLogsSize);
        int additionalNonPriorityLogs = additionalLogs;
        if (additionalNonPriorityLogs > 0) {
            nonPriorityMainLogs = getNonPriorityWorkLogsForAccount(account, logType, additionalLogs, startDate, endDate, includeHiddenLogs, true);
            if (nonPriorityMainLogs != null && !nonPriorityMainLogs.isEmpty()) {
                allNonPriorityLogs = convertMainLogToPojo(nonPriorityMainLogs);
            }
            if (logType.contains(WorkLogTypes.WORK_LOG_DATA_CHANGE)) {
                nonPriorityDcLogs = getAllDataChangeLogs(account, additionalLogs, startDate, endDate);
                allNonPriorityLogs.addAll(nonPriorityDcLogs);
            }
            finalNonPrioritySortedByTimeStamp = getLogListSortedByTimeStamp(allNonPriorityLogs, additionalNonPriorityLogs);
            //allPriorityLogs.addAll(finalNonPrioritySortedByTimeStamp);
            finalLogsPojo.addAll(finalNonPrioritySortedByTimeStamp);
        }

        return finalLogsPojo;
    }

    public List<WorkLogPojo> getAllLogForAccountBasedOnPriority(Account account, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        List<WorkMainLog> nonPriorityMainLogs = null;
        List<WorkMainLog> priorityMainLogs = new ArrayList<>();
        List<WorkMainLog> managementReviewLogs = null;
        List<WorkLogPojo> priorityDCLogs = new ArrayList<>();
        List<WorkLogPojo> nonPriorityDcLogs = null;
        List<WorkLogPojo> allNonPriorityLogs = new ArrayList<>();
        List<WorkLogPojo> allPriorityLogs = new ArrayList<>();
        List<WorkLogPojo> mgmtPojo = new ArrayList<>();
        List<WorkLogPojo> finalLogsPojo = new ArrayList<>();
        List<WorkLogPojo> finalNonPrioritySortedByTimeStamp = new ArrayList<>();
        int priorityMainLogsSize = 0, priorityDCLogsSize = 0, additionalNonPriorityLogs;

        managementReviewLogs = getManagementReviewLogs(account, null, maxCount, startDate, endDate, includeHiddenLogs);

        int additionalLogs = maxCount;

        if (managementReviewLogs != null && !managementReviewLogs.isEmpty()) {
            mgmtPojo = convertMainLogToPojo(managementReviewLogs);
            additionalLogs = additionalLogs - managementReviewLogs.size();
        }
        finalLogsPojo.addAll(mgmtPojo);

        Set<Integer> logTypesSet = null;
        try {
            logTypesSet = WorkLogTypes.getAllLogTypes().keySet();
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException ex) {
        }
        List<Integer> logTypes = new ArrayList<>();
        logTypes.addAll(logTypesSet);

        if (additionalLogs > 0) {
            priorityMainLogs = getPriorityWorkLogsForAccount(account, logTypes, additionalLogs, startDate, endDate, includeHiddenLogs);
        }

        if (priorityMainLogs != null && !priorityMainLogs.isEmpty()) {
            allPriorityLogs = convertMainLogToPojo(priorityMainLogs);
            priorityMainLogsSize = priorityMainLogs.size();
        }

        priorityDCLogs = getAllPriorityDataChangeLogs(account, additionalLogs, startDate, endDate);

        if (priorityDCLogs != null && !priorityDCLogs.isEmpty()) {
            allPriorityLogs.addAll(priorityDCLogs);
            priorityDCLogsSize = priorityDCLogs.size();
        }

        finalLogsPojo.addAll(getLogListSortedByTimeStamp(allPriorityLogs, additionalLogs));
        additionalLogs = additionalLogs - (priorityDCLogsSize + priorityMainLogsSize);
        additionalNonPriorityLogs = additionalLogs;
        if (additionalNonPriorityLogs > 0) {
            nonPriorityMainLogs = getNonPriorityWorkLogsForAccount(account, logTypes, additionalLogs, startDate, endDate, includeHiddenLogs, false);

            if (nonPriorityMainLogs != null && !nonPriorityMainLogs.isEmpty()) {
                allNonPriorityLogs = convertMainLogToPojo(nonPriorityMainLogs);
            }
            if (logTypes.contains(WorkLogTypes.WORK_LOG_DATA_CHANGE)) {
                nonPriorityDcLogs = getAllDataChangeLogs(account, additionalLogs, startDate, endDate);
                allNonPriorityLogs.addAll(nonPriorityDcLogs);
            }

            finalNonPrioritySortedByTimeStamp = getLogListSortedByTimeStamp(allNonPriorityLogs, additionalLogs);
            //allPriorityLogs.addAll(finalNonPrioritySortedByTimeStamp);
            finalLogsPojo.addAll(finalNonPrioritySortedByTimeStamp);

        }

        return finalLogsPojo;
    }

    public List<WorkLogPojo> getLogListSortedByTimeStamp(List<WorkLogPojo> finalLogsList, int maxCount) {
        if (finalLogsList.size() > 0) {
            Collections.sort(finalLogsList, new Comparator<WorkLogPojo>() {
                @Override
                public int compare(WorkLogPojo clog1, WorkLogPojo clog2) {
                    return clog2.getCreateTimestamp().compareTo(clog1.getCreateTimestamp());
                }
            });
        } else {
            return finalLogsList;
        }

        if (maxCount > 0) {
            if (maxCount < finalLogsList.size()) {
                finalLogsList = new ArrayList<>(finalLogsList.subList(0, maxCount));
            }
        }
        return finalLogsList;
    }

    @SuppressWarnings("unchecked")
    public List<WorkLogPojo> getAllPriorityDataChangeLogs(Account account, int maxCount, LocalDate startDate, LocalDate endDate) {
        String sqlQuery = "SELECT MIN(create_timestamp) as createTime , string_agg(attribute_name||' OldValue : '||coalesce(old_value,' ')||' New Value : '||coalesce(new_value,' '),', ')  as LogNote , class_name , type , log_desc, agent_username as username, is_priority"
                + " FROM svc.sv_data_change_log "
                + " WHERE account_pk = ? AND hidden = 'N' AND is_priority = 'Y'"
                + " GROUP BY group_code , class_name , type , log_desc, agent_username, is_priority ";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" HAVING MIN(create_timestamp) BETWEEN ? AND ? ");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY MIN(create_timestamp) DESC");
        Query query = entityManager.createNativeQuery(sqlQuery);
        query.setParameter(1, account.getPk());
        if (startDate != null && endDate != null) {
            Timestamp startDateTime = new Timestamp(startDate.toDateTimeAtStartOfDay().getMillis());
            Timestamp endDateTime = new Timestamp(endDate.plusDays(1).toDateTimeAtStartOfDay().plusMillis(-1).getMillis());
            query.setParameter(2, startDateTime);
            query.setParameter(3, endDateTime);
        }
        List<Object[]> res = null;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        List<WorkLogPojo> workList = new ArrayList<>();
        for (Object[] objArray : res) {
            WorkLogPojo workLogPojo = new WorkLogPojo();
            Timestamp sqlTimeStamp = (Timestamp) objArray[0];
            DateTime sqlDate = new DateTime(sqlTimeStamp.getTime());
            workLogPojo.setCreateTimestamp(sqlDate.toLocalDateTime());
            String logNote = (String) objArray[1];
            String className = (String) objArray[2];
            Integer type = (Integer) objArray[3];
            String logDesc = (String) objArray[4];
            String agentUserName = (String) objArray[5];
            Boolean isPriority = (Boolean) objArray[6];
            StringBuilder finalLogNoteBuilder = new StringBuilder();
            if (logDesc != null && logDesc.trim() != "") {
                finalLogNoteBuilder = finalLogNoteBuilder.append(logDesc).append(" ").append(logNote);
            } else {
                finalLogNoteBuilder = finalLogNoteBuilder.append(className).append(" ").append(logNote);
            }
            if (type == 0) {
                workLogPojo.setLogNote("Created " + finalLogNoteBuilder.toString());
            }
            if (type == 1) {
                workLogPojo.setLogNote("Updated " + finalLogNoteBuilder.toString());
            }
            if (type == 2) {
                workLogPojo.setLogNote("Deleted " + finalLogNoteBuilder.toString());
            }

            workLogPojo.setAccountPk(account.getPk());
            if (isPriority != null && isPriority) {
                workLogPojo.setPriority(isPriority);
            } else {
                workLogPojo.setPriority(false);
            }
            workLogPojo.setUserName(agentUserName);
            workList.add(workLogPojo);
            WorkLogData workLogData = new WorkLogData();
            workLogData.setLogType(WorkLogTypes.WORK_LOG_DATA_CHANGE);
            workLogData.setLogTypeDesc(WorkLogTypes.getLogTypeDesc(workLogData.getLogType()));
            workLogPojo.setWorkLogData(workLogData);
        }
        return workList;
    }

    public List<WorkMainLog> getPriorityWorkLogsForAccount(Account account, List logType, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        //exclude Management logs from this query as they are exclusively obtained using getManagementReviewLogs
        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk and s.logType in :logType and s.logType != :mgmtLogType and ((s.priority = true and s.expireDate >= :expireDate) or (s.priority = true and s.expireDate is null )) and s.deleted = false";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        query.setParameter("logType", logType);
        query.setParameter("mgmtLogType", WorkLogTypes.WORK_LOG_MANAGMENT_REVIEW);
        query.setParameter("expireDate", LocalDate.now());
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }

        List<WorkMainLog> res = null;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkMainLog> getNonPriorityWorkLogsForAccount(Account account, List logType, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs, boolean includeLogype) {
        //exclude Management logs from this query as they are exclusively obtained using getManagementReviewLogs
        String sqlQuery = "SELECT s FROM WorkMainLog s WHERE s.accountPk = :accountPk and s.logType != :mgmtLogType and ((s.priority = false and s.expireDate >= :expireDate) or (s.priority = false and s.expireDate is null )) and s.deleted = false";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        if (includeLogype) {
            sqlQuery = sqlQuery.concat(" AND s.logType in :logType");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        query.setParameter("expireDate", LocalDate.now());
        query.setParameter("mgmtLogType", WorkLogTypes.WORK_LOG_MANAGMENT_REVIEW);
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        if (includeLogype) {
            query.setParameter("logType", logType);
        }
        List<WorkMainLog> res = null;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkMainLog> getManagementReviewLogs(Account account, List logType, int maxCount, LocalDate startDate, LocalDate endDate, Boolean includeHiddenLogs) {
        String sqlQuery = "SELECT s FROM WorkManagementReviewLog s WHERE s.accountPk = :accountPk and s.reviewed = false and s.deleted = false";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" AND s.createTimestamp BETWEEN :startDate AND :endDate");
        }
        if (!includeHiddenLogs) {
            sqlQuery = sqlQuery.concat(" AND (s.isHidden=false OR s.isHidden is NULL)");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY s.createTimestamp DESC");
        TypedQuery<WorkMainLog> query = entityManager.createQuery(sqlQuery, WorkMainLog.class);
        query.setParameter("accountPk", account.getPk());
        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate.toLocalDateTime(LocalTime.MIDNIGHT));
            query.setParameter("endDate", endDate.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT).plusMillis(-1));
        }
        List<WorkMainLog> res = null;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public List<WorkLogPojo> getAllDataChangeLogs(Account account) {
        return getAllDataChangeLogs(account, 0, null, null);
    }

    @SuppressWarnings("unchecked")
    public List<WorkLogPojo> getAllDataChangeLogs(Account account, int maxCount, LocalDate startDate, LocalDate endDate) {

        /*Select date_trunc('second', create_timestamp)  , string_agg(attribute_name||' OldValue : '||coalesce(old_value,'')||' New Value : '||coalesce(new_value,''),', ')  as LogNote , class_name from svc.sv_data_change_log where account_pk = 1 
         group by date_trunc('second', create_timestamp) , class_name
         having date_trunc('second', create_timestamp) BETWEEN current_timestamp - 8 * interval '1 day' AND current_timestamp
         order by date_trunc('second', create_timestamp) desc*/
        /*
         IMPLEMENTATION WITHOUT USING NATIVE QUERY
         */
        /*CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
         Root from = criteriaQuery.from(DataChangeLog.class);
         Expression<String> path = from.get("attributeName");
         Path timestamp = from.get("createTimestamp");
         Path className = from.get("className");
         ParameterExpression<Long> p = criteriaBuilder.parameter(Long.class,"id");
         Expression<String> functionStringAgg = criteriaBuilder.function( "string_agg",String.class,path,criteriaBuilder.parameter(String.class,"delimiter"));
         CriteriaQuery<Object> select = criteriaQuery.multiselect(timestamp,functionStringAgg);
         CriteriaQuery<Object> where = criteriaQuery.where(criteriaBuilder.equal(from.get("accountPk"),p));
         CriteriaQuery<Object> groupBy = select.groupBy(timestamp,className);
         TypedQuery<Object> typedQuery = entityManager.createQuery(select);
         typedQuery.setParameter("id", account.getPk());
         typedQuery.setParameter("delimiter", ", ");
         System.out.println("QUERY" + typedQuery.unwrap(org.hibernate.Query.class).getQueryString());
         List listActual = typedQuery.getResultList();
         */
        String sqlQuery = "SELECT MIN(create_timestamp) as createTime , string_agg(attribute_name||' OldValue : '||coalesce(old_value,' ')||' New Value : '||coalesce(new_value,' '),', ')  as LogNote , class_name , type , log_desc, agent_username as username, is_priority"
                + " FROM svc.sv_data_change_log "
                + " WHERE account_pk = ? AND hidden = 'N' AND (is_priority = 'N' OR is_priority is null)"
                + " GROUP BY group_code , class_name , type , log_desc, agent_username, is_priority ";
        if (startDate != null && endDate != null) {
            sqlQuery = sqlQuery.concat(" HAVING MIN(create_timestamp) BETWEEN ? AND ? ");
        }
        sqlQuery = sqlQuery.concat(" ORDER BY MIN(create_timestamp) DESC");
        Query query = entityManager.createNativeQuery(sqlQuery);
        query.setParameter(1, account.getPk());
        if (startDate != null && endDate != null) {
            Timestamp startDateTime = new Timestamp(startDate.toDateTimeAtStartOfDay().getMillis());
            Timestamp endDateTime = new Timestamp(endDate.plusDays(1).toDateTimeAtStartOfDay().plusMillis(-1).getMillis());
            query.setParameter(2, startDateTime);
            query.setParameter(3, endDateTime);
        }
        List<Object[]> res = null;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        List<WorkLogPojo> workList = new ArrayList<>();
        for (Object[] objArray : res) {
            WorkLogPojo workLogPojo = new WorkLogPojo();
            Timestamp sqlTimeStamp = (Timestamp) objArray[0];
            DateTime sqlDate = new DateTime(sqlTimeStamp.getTime());
            workLogPojo.setCreateTimestamp(sqlDate.toLocalDateTime());
            String logNote = (String) objArray[1];
            String className = (String) objArray[2];
            Integer type = (Integer) objArray[3];
            String logDesc = (String) objArray[4];
            String agentUserName = (String) objArray[5];
            Boolean isPriority = (Boolean) objArray[6];
            StringBuilder finalLogNoteBuilder = new StringBuilder();
            if (logDesc != null && logDesc.trim() != "") {
                finalLogNoteBuilder = finalLogNoteBuilder.append(logDesc).append(" ").append(logNote);
            } else {
                finalLogNoteBuilder = finalLogNoteBuilder.append(className).append(" ").append(logNote);
            }
            if (type == 0) {
                workLogPojo.setLogNote("Created " + finalLogNoteBuilder.toString());
            }
            if (type == 1) {
                workLogPojo.setLogNote("Updated " + finalLogNoteBuilder.toString());
            }
            if (type == 2) {
                workLogPojo.setLogNote("Deleted " + finalLogNoteBuilder.toString());
            }

            workLogPojo.setAccountPk(account.getPk());
            if (isPriority != null && isPriority) {
                workLogPojo.setPriority(isPriority);
            } else {
                workLogPojo.setPriority(false);
            }
            workLogPojo.setUserName(agentUserName);
            workList.add(workLogPojo);
            WorkLogData workLogData = new WorkLogData();
            workLogData.setLogType(WorkLogTypes.WORK_LOG_DATA_CHANGE);
            workLogData.setLogTypeDesc(WorkLogTypes.getLogTypeDesc(workLogData.getLogType()));
            workLogPojo.setWorkLogData(workLogData);
        }
        return workList;
    }

    public List<DataChangeLog> getAllViewableDataChangeLogs(Account account, int maxCount) {
        TypedQuery<DataChangeLog> query = entityManager.createQuery("SELECT s FROM DataChangeLog s WHERE s.accountPk = :accountPk and s.hidden = false and s.deleted = false", DataChangeLog.class);
        query.setParameter("accountPk", account.getPk());

        List<DataChangeLog> res;
        if (maxCount > 0) {
            res = query.setMaxResults(maxCount).getResultList();
        } else {
            res = query.getResultList();
        }
        return res;
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    public String getAgentUsername() {
        UserData userData = (UserData) ThreadAttributes.get("agent.username");
        String agentUsername = null;
        if (userData != null) {
            agentUsername = userData.getUserName();
        }
        if (agentUsername == null) {
            agentUsername = WorkLogConstants.SYSTEM_DEFAULT_USERNAME;
        }
        return agentUsername;
    }

    public void removeWorkLogWithPk(long logPk) {
        entityManager.createQuery("UPDATE WorkMainLog s SET s.deleted = true WHERE s.pk = :logPk").setParameter("logPk", logPk).executeUpdate();
    }

    public void createWorkCallLog(WorkCallLog cdrLog) {
        entityManager.persist(cdrLog);
    }

    public List<WorkCallLog> getWorkCallLogByAccountPk(long accountPk, LocalDate fromDate, LocalDate toDate) {
        TypedQuery<WorkCallLog> query;
        if (fromDate != null && toDate != null) {
            query = entityManager.createNamedQuery("WorkCallLog.getCallLogsWithDates", WorkCallLog.class);
            query.setParameter("fromDate", new LocalDateTime(fromDate.toDateTimeAtStartOfDay()));
            query.setParameter("toDate", new LocalDateTime(toDate.toDateTimeAtStartOfDay().plusDays(1).plusMillis(-1)));
        } else {
            query = entityManager.createNamedQuery("WorkCallLog.getCallLogs", WorkCallLog.class);
        }
        query.setParameter("accountPk", accountPk);
        return query.getResultList();
    }

    //@Async
    public LocalDateTime locateReviewLogByAccountPkAndAgent(long accountPk, String agentUsername) {
        LocalDateTime time;
        try {
            Query query = entityManager.createQuery("SELECT MAX(s.createTimestamp) FROM WorkReviewLog s WHERE s.accountPk = :accountPk and s.agentUsername = :agentUsername");
            query.setParameter("accountPk", accountPk);
            query.setParameter("agentUsername", agentUsername);
            time = (LocalDateTime) query.getSingleResult();
            LOG.info("time:{}", time);
        } catch (Exception ex) {
            time = null;
            LOG.info("Exception extracting result addReviewLog:" + ex);
        }
        return time;
    }

    @Async
    @Transactional
    public void addReviewedLog(long accountPk, UserData userData, Boolean isHidden) {
        LOG.info("ThreadAttributes: Implementing Aync ... Thread id: {} and transaction id: {}", ThreadAttributes.getString("current.thread.id"), ThreadAttributes.getString("current.transaction.id"));
        ThreadAttributes.set("agent.username", userData);
        Account account = accountRepository.findAccountByPk(accountPk);
        account.getAccountData().setLastReviewedDateTime(LocalDateTime.now());
        String userName = getAgentUsername();
        LOG.info("userName:" + userName);
        LocalDateTime existingReviewedLogTime = null;
        try {
            existingReviewedLogTime = locateReviewLogByAccountPkAndAgent(accountPk, userName);
        } catch (Exception ex) {
            LOG.info("Exception in HQL for addReviewdog:" + ex);
        }
        WorkReviewLog log = new WorkReviewLog();
        if (existingReviewedLogTime == null) {
            persistReviewLog(log, accountPk, isHidden);
        } else {
            if (existingReviewedLogTime.isBefore(LocalDateTime.now().minusMinutes(worklLogCofigs.getLAST_REVIEWED()))) {
                LOG.info("persisted new log as the last log for this user was created before 5 minutes");
                persistReviewLog(log, accountPk, isHidden);
            } else {
                LOG.info("A review log was created for the account with pk:{} in the last {} minutes for the agent {}", accountPk, worklLogCofigs.getLAST_REVIEWED(), userName);
            }
        }
    }

    public void persistReviewLog(WorkReviewLog log, long accountPk, boolean isHidden) {
        log.setAgentUsername(getAgentUsername());
        log.setDescription("Account has been reviewed");
        log.setAccountPk(accountPk);
        log.setLogType(WorkLogTypes.WORK_LOG_REVIEWED);
        log.setIsHidden(isHidden);
        entityManager.persist(log);
    }

    public List<DataChangeLog> getDataChangeLogsForAccountAndProperty(long accountPk, String propertyName) {
        return entityManager.createQuery("SELECT s FROM DataChangeLog s WHERE s.accountPk = :accountPk AND s.attributeName = :attributeName", DataChangeLog.class).
                setParameter("accountPk", accountPk).
                setParameter("attributeName", propertyName).
                getResultList();

    }

    public void setContactTimestamp(Account account, CallDispositionCode dispositionCode) {
        account.getAccountData().setLastContactTimestamp(LocalDateTime.now());
        LOG.info("Contact Made for DispositionCode string {}", dispositionCode.getCode());
        if (!dispositionCode.getCode().equalsIgnoreCase("3pLM") && !dispositionCode.getCode().equalsIgnoreCase("LM")) {
            LOG.info("Set myQueue timestamp for disposition code {}", dispositionCode.getCode());
            account.getAccountData().setMyQueuelastContactTimestamp(LocalDateTime.now());
        }
        LOG.info("Last Contact Timestamp: {} and MyQueue timestamp: {}", account.getAccountData().getLastContactTimestamp(), account.getAccountData().getMyQueuelastContactTimestamp());
    }

    public List<WorkMainLog> getAllExpiredPriorityLogs() {
        List<WorkMainLog> logsList = new ArrayList<>();
        TypedQuery<WorkMainLog> query = entityManager.createQuery("SELECT s FROM WorkMainLog s WHERE s.expireDate < :today and s.priority = true", WorkMainLog.class);
        query.setParameter("today", LocalDate.now());
        logsList = query.getResultList();
        return logsList;
    }

    public void resetPriority(long workLogPk) {
        String queryStr = "UPDATE WorkMainLog s SET s.priority=false where s.pk=:logPk";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("logPk", workLogPk);
        query.executeUpdate();
    }

    public int updateAccountPkForMainLogsWithCallUUID(String callUUID, long newAccountPk) {
        return entityManager.createNativeQuery("update sti.work_main_log set account_pk = :newAccountPk where call_uuid = :callUUID").setParameter("newAccountPk", newAccountPk).setParameter("callUUID", callUUID).executeUpdate();
    }

}
