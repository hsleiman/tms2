/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.tms;

import com.objectbrains.config.CompanyInfo;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.sti.constants.DialPlanContext;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.dialer.CallDetailRecord;
import com.objectbrains.sti.db.entity.base.dialer.InboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.base.dialer.SpeechToText;
import com.objectbrains.sti.db.repository.StiAgentRepository;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.dialer.BIMessageRepository;
import com.objectbrains.sti.db.repository.dialer.DialerQueueRepository;
import com.objectbrains.sti.db.repository.dialer.StiCallDetailRecordRepository;
import com.objectbrains.sti.db.repository.disposition.CallDispositionRepository;
import com.objectbrains.sti.db.repository.qaform.CallQualityManagementRepository;
import com.objectbrains.sti.embeddable.DialerQueueDetails;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.pojo.*;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.utility.PhoneUtils;
import java.sql.Timestamp;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author David
 */
@Service
@Transactional
public class TMSService {

    public static final Logger LOG = LoggerFactory.getLogger(TMSService.class);
//    @ConfigContext
//    private ConfigurationUtility configd;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private StiCallDetailRecordRepository cdrRepo;
    @Autowired
    private CallQualityManagementRepository callQualityRepo;
    @Autowired
    private DialerQueueRepository dqRepo;
    @Autowired
    private StiAgentRepository agentRepo;
    @Autowired
    private DialerQueueService dqService;
    @PersistenceContext
    private EntityManager entityManager;
    @ConfigContext
    private CompanyInfo companyInfo;
    @Autowired
    private BIMessageRepository biMessageRepository ;
    @Autowired
    private CallDispositionRepository dispositionRepository;

    public TMSBasicAccountInfo getBasicAccountInfoForTMS(long accountPk) {
        Account account = accountRepo.findAccountByPk(accountPk);

        //Figure out what values we actually need from STI to give to TMS
        //SvLoanTerm svLoanTerm = loanRepo.getCurrentTerm(svLoan);
        TMSBasicAccountInfo bai = new TMSBasicAccountInfo();
        bai.setAccountPk(accountPk);

//        bai.setPrincipalBalance(svLoanTerm.getCurrentBalance());
//        SvDueAmount earliestUnpaidDueAmount = loanTermRepo.getEarliestDueAmountNotPaidAdequate(svLoanTerm.getPk());
//        if (earliestUnpaidDueAmount != null && earliestUnpaidDueAmount.getDueAmount().getDueDate().isBefore(LocalDate.now())) {
//            bli.setNextDueDate(earliestUnpaidDueAmount.getDueAmount().getDueDate());
//        } else {
//            SvDueAmount due = loanTermRepo.getNextDue(svLoanTerm.getPk());
//            if (due != null) {
//                bli.setNextDueDate(due.getDueAmount().getDueDate());
//            }
//        }
//        if (bli.getNextDueDate() == null) {
//            bli.setNextDueDate(new LocalDate().minusDays(1));
//        }
//        if (svLoan.getSvLoanStatus().getLoanStatus() != null && svLoan.getSvLoanStatus().getLoanStatus() == LoanStatusCode.STATUS_PAID_OFF) {
//            bli.setPaidOff(true);
//        } else {
//            bli.setPaidOff(false);
//        }
        return bai;
    }

    public void setBestTimeToCallForAccount(long accountPk) throws StiException {

        Account account = accountRepo.findAccountByPk(accountPk);
        BestTimeToCallPojo pojo = getBestTimeToCall(account.getPk(), 60.0);
        if (pojo.getGoodCalls() < 10) {
            LOG.info("bestTime to call for account {} is set to {}", account.getPk(), pojo.getBestHitRatioTime());
            account.getAccountData().setBestTimeToCall(pojo.getBestHitRatioTime());
        } else {
            LOG.info("bestTime to call for account {} is set to {}", account.getPk(), pojo.getMostHitsTime());
            account.getAccountData().setBestTimeToCall(pojo.getMostHitsTime());
        }

    }

    public BestTimeToCallPojo getBestTimeToCall(long accountPk, Double rangeInMinutes) {
        List<CallDetailRecord> cdrList = cdrRepo.locateAllCDRByAccountPk(accountPk);
        rangeInMinutes = rangeInMinutes / 2;
        LOG.info("Calculating best time to call for account {}", accountPk);
        BestTimeToCallPojo pojo = new BestTimeToCallPojo();
        if (cdrList.isEmpty()) {
            LOG.info("Returned null because there are no call detail records with the provided accountPk {}", accountPk);
            return null;
        }
        ArrayList<BestTimeToCallCluster> bestTimeToCallClusters = getBestCallTimes(cdrList, (int) (Math.log(cdrList.size()) / Math.log(2)), rangeInMinutes);
        if (bestTimeToCallClusters.size() == 2 && bestTimeToCallClusters.get(1) != null && bestTimeToCallClusters.get(0) != null) {
            pojo.setBestHitRatioStartTime(bestTimeToCallClusters.get(1).getCenterNode().getStartTime().minusMinutes(rangeInMinutes.intValue()));
            pojo.setBestHitRatioEndTime(bestTimeToCallClusters.get(1).getCenterNode().getStartTime().plusMinutes(rangeInMinutes.intValue()));
            pojo.setMostHitsStartTime(bestTimeToCallClusters.get(0).getCenterNode().getStartTime().minusMinutes(rangeInMinutes.intValue()));
            pojo.setMostHitsEndTime(bestTimeToCallClusters.get(0).getCenterNode().getStartTime().plusMinutes(rangeInMinutes.intValue()));
            pojo.setGoodCalls(bestTimeToCallClusters.get(1).getGoodCalls());
            pojo.setBadCalls(bestTimeToCallClusters.get(1).getBadCalls());
            pojo.setHitRatio(pojo.getGoodCalls() / (pojo.getBadCalls() + 1));
            pojo.setMostGoodCalls(bestTimeToCallClusters.get(0).getGoodCalls());
            pojo.setBestHitRatioTime();
            pojo.setMostHitsTime();
        } else {
            LOG.error("There was a problem getting the best time to call. Most likely there was no good calls associated to this account :{}", accountPk);
            pojo = null;
        }
        return pojo;
    }

    private ArrayList<BestTimeToCallCluster> getBestCallTimes(List<CallDetailRecord> cdrList, int clusterMinSize, double rangeInMinutes) {
        ArrayList<BestTimeToCallCluster> clusterList = new ArrayList<>();
        ArrayList<BestTimeToCallCluster> bestBestTimeToCallClusters = new ArrayList<>();
        BestTimeToCallCluster newBestTimeToCallCluster = null;
        for (CallDetailRecord node : cdrList) {
            if (isSuccessfulCall(node.getCallDurationInMilliSec())) {
                HashSet<CallDetailRecord> nodeHash = new HashSet<>(cdrList);
                nodeHash.remove(node);
                newBestTimeToCallCluster = exploreThisNode(node, nodeHash, clusterMinSize, rangeInMinutes);
                if (newBestTimeToCallCluster != null) {
                    clusterList.add(newBestTimeToCallCluster);
                }
            }
        }
        if (clusterList.isEmpty() && clusterMinSize != 0) {
            return getBestCallTimes(cdrList, (int) (clusterMinSize * (2.0 / 3.0)), rangeInMinutes);
        }

        double hits = 0;
        newBestTimeToCallCluster = null;
        for (BestTimeToCallCluster thisBestTimeToCallCluster : clusterList) {
            if (thisBestTimeToCallCluster.getGoodCalls() > hits) {
                hits = thisBestTimeToCallCluster.getGoodCalls();
                newBestTimeToCallCluster = thisBestTimeToCallCluster;
            }
        }
        if (newBestTimeToCallCluster != null) {
            bestBestTimeToCallClusters.add(newBestTimeToCallCluster);
        }
        double hitRate = 0;
        newBestTimeToCallCluster = null;
        for (BestTimeToCallCluster thisBestTimeToCallCluster : clusterList) {
            if ((thisBestTimeToCallCluster.getGoodCalls() / (thisBestTimeToCallCluster.getBadCalls() + 1)) > hitRate) {
                hitRate = thisBestTimeToCallCluster.getGoodCalls() / (thisBestTimeToCallCluster.getBadCalls() + 1);
                newBestTimeToCallCluster = thisBestTimeToCallCluster;
            }
        }
        if (newBestTimeToCallCluster != null) {
            bestBestTimeToCallClusters.add(newBestTimeToCallCluster);
        }
        return bestBestTimeToCallClusters;
    }

    private BestTimeToCallCluster exploreThisNode(CallDetailRecord centerNode, HashSet<CallDetailRecord> nodeSet, int clusterMinSize, double rangeInMinutes) {
        BestTimeToCallCluster tempBestTimeToCallCluster = new BestTimeToCallCluster();
        tempBestTimeToCallCluster.goodCallsInc();
        tempBestTimeToCallCluster.setCenterNode(centerNode);
        tempBestTimeToCallCluster.setTimeRangeInMinutes(rangeInMinutes);
        for (CallDetailRecord node : nodeSet) {
            if (isWithinTimeRange(node.getStartTime(), tempBestTimeToCallCluster.getCenterNode().getStartTime(), rangeInMinutes)) {
                if (isSuccessfulCall(node.getCallDurationInMilliSec())) {
                    tempBestTimeToCallCluster.goodCallsInc();
                } else {
                    tempBestTimeToCallCluster.badCallsInc();

                }
            }
        }
        if (tempBestTimeToCallCluster.getGoodCalls() >= clusterMinSize) {
            return tempBestTimeToCallCluster;
        } else {
            return null;
        }

    }

    private boolean isSuccessfulCall(Long callDurationInMSeconds) {
        if (callDurationInMSeconds == null) {
            return false;
        }
        return callDurationInMSeconds >  10000d;
    }

    public boolean isWithinTimeRange(LocalDateTime newNodeTime, LocalDateTime centerNodeTime, double rangeInMinutes) {
        long diffInMillis = newNodeTime.getMillisOfDay() - centerNodeTime.getMillisOfDay();
        //long diffInMillis = newNodeTime.toDateTime().getMillis() - centerNodeTime.toDateTime().getMillis();
        long maxDifference = (long) (rangeInMinutes * 60 * 1000);
        return Math.abs(diffInMillis) < maxDifference;
    }

    //    public List<WorkCallLogPojo> getAllCallHistory(CallHistoryCriteria callHistoryCriteria) {
//        List<CallDetailRecord> cdrList = cdrRepo.getAllCDR(callHistoryCriteria.getFromDate(), callHistoryCriteria.getToDate(),
//                callHistoryCriteria.getCallerPhoneNumber(), callHistoryCriteria.getCalleePhoneNumber(), callHistoryCriteria.getCallType(),
//                callHistoryCriteria.getAccountPk(), callHistoryCriteria.getDialerCall(), callHistoryCriteria.getUserDisposition(),
//                callHistoryCriteria.getPageNumber(), callHistoryCriteria.getPageSize());
//        return buildCallHistory(cdrList);
//    }
    private List<WorkCallLogPojo> buildCallHistory(List<CallDetailRecord> cdrList) {
//        UserData ud = (UserData) ThreadAttributes.get("agent.username");
//        String agentUserName = ud.getUserName();
        List<WorkCallLogPojo> callLogPojoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cdrList)) {
            List<Long> recordPks = new ArrayList<>(cdrList.size());
            for (CallDetailRecord record : cdrList) {
                recordPks.add(record.getPk());
            }

            Map<Long, List<CallLogLeg>> callLogLegMap = new HashMap<>();
            List<CallLogLeg> results = entityManager.createQuery("SELECT new " + CallLogLeg.class
                    .getName()
                    + "(log.CallDetailRecord.pk, log.callDurationInMilliSec, log.startTime, "
                    + "case when log.dialer = true then log.destinationNumber else log.callerIdNumber end,"
                    + "log.callerIdName, "
                    + "case when coalesce(log.calleeIdNumber, '') = '' then log.destinationNumber else log.calleeIdNumber end, "
                    + "log.calleeIdName )"
                    + "FROM CollectionCallLog log "
                    //+ "WHERE log.context in ('" + DialPlanContext.AGENT_DP.name() + "', '" + DialPlanContext.DQ_DP.name() + "') "
                    + "WHERE log.context = '" + DialPlanContext.AGENT_DP.name() + "'"
                    + "AND log.callDetailRecord.pk in (:recordPks)"
                    + "AND log.callDurationInMilliSec is not null "
                    + "ORDER BY log.startTime desc, log.context desc", CallLogLeg.class
            )
                    .setParameter("recordPks", recordPks)
                    .getResultList();

            for (CallLogLeg callLogLeg : results) {
                Long recordPk = callLogLeg.getCallDetailRecordPk();
                List<CallLogLeg> legs = callLogLegMap.get(recordPk);
                if (legs == null) {
                    legs = new ArrayList<>();
                    callLogLegMap.put(recordPk, legs);
                }
                legs.add(callLogLeg);
            }

            for (CallDetailRecord cdr : cdrList) {
                WorkCallLogPojo pojo = new WorkCallLogPojo();
                SpeechToText sTT = cdrRepo.locateSpeeechToTextForUuid(cdr.getCallUUID());
                if (sTT != null) {
                    if (sTT.getKeywordsInText() == null) {
                        pojo.setKeyword("");
                    } else {
                        pojo.setKeyword(sTT.getKeywordsInText());
                    }
                    if (sTT.getBadLanguageList() == null) {
                        pojo.setBadLanguageText("");
                    } else {
                        pojo.setBadLanguageText(sTT.getBadLanguageList());
                    }
                    if (sTT.isBadLanguageInText() == null) {
                        pojo.setBadLanguage(Boolean.FALSE);
                    } else {
                        pojo.setBadLanguage(sTT.isBadLanguageInText());
                    }
                    pojo.setKeywordDetected(!pojo.getKeyword().isEmpty());
                    pojo.setBadLanguageText(sTT.getBadLanguageList());
                    pojo.setKeywordPriority(sTT.getKeywordPriority());
                    pojo.setBadLanguagePriority(sTT.getBadLanguagePriority());
                    pojo.setBadBehavior(pojo.isBadLanguage());
                }
                pojo.setAccountPk(cdr.getAccountPk());
                pojo.setUUID(cdr.getCallUUID());
                pojo.setCallDirection(cdr.getCallDirection());
                pojo.setCallRecordURL(cdr.getCallRecordingUrl());
                pojo.setFirstCallTime(cdr.getStartTime());
                //pojo.setBadBehavior(cdr.isBadBehavior());
                //pojo.setBadLanguage(cdr.isBadLanguage());
                //pojo.setBadLanguageText(cdr.getBadLanguageText());
                pojo.setCallDisposition(cdr.getCallDisposition());
                pojo.setCountOfQMForms(callQualityRepo.getAllCallQualityManagementEvaluation(cdr.getCallUUID()).size());

                pojo.setIsSpeechToTextRequested(cdr.isSpeechToTextRequested());
                pojo.setIsSpeechToTextCompleted(cdr.isSpeechToTextCompleted());
                if (cdr.getUserDisposition() != null) {
                    if (cdr.getUserDisposition().equals("Unknown-null")) {
                        pojo.setUserDisposition("");
                    } else {
                        pojo.setUserDisposition(cdr.getUserDisposition());
                    }
                } else {
                    pojo.setUserDisposition("");
                }
                pojo.setFullCallDuration(cdr.getCallDurationInMilliSec());
                pojo.setDialer(cdr.isDialer() != null && cdr.isDialer());
//                pojo.setAgentUserName(agentUserName);
                List<CallLogLeg> callLogLegList = callLogLegMap.get(cdr.getPk());
                if (!CollectionUtils.isEmpty(callLogLegList)) {
                    Collections.sort(callLogLegList);
                    pojo.setLegList(callLogLegList);
                    callLogPojoList.add(pojo);
                } else {
                    if (cdr.getStartTime() != null && cdr.getCallDurationInMilliSec() != null) {
                        CallLogLeg callLogLeg = new CallLogLeg();
                        callLogLeg.setCallDetailRecordPk(cdr.getPk());
                        callLogLeg.setCallDuration(cdr.getCallDurationInMilliSec());
                        callLogLeg.setCalleeName(cdr.getCalleeIdName());
                        callLogLeg.setCallerName(cdr.getCallerIdName());
                        String callerNum = StringUtils.isBlank(cdr.getCallerIdNumber()) ? cdr.getDestinationNumber() : cdr.getCallerIdNumber();
                        callLogLeg.setFromPhoneNumber(StringUtils.isBlank(callerNum) ? "Dialer" : callerNum);
                        callLogLeg.setToPhoneNumber(cdr.getCalleeIdNumber());
                        callLogLeg.setStartCallTime(cdr.getStartTime());
                        callLogLeg.setCallDisposition(cdr.getCallDisposition());
                        callLogLeg.setUserDisposition(pojo.getUserDisposition());
//                        callLogLeg.setAgentUserName(agentUserName);
                        callLogLegList = new ArrayList<>();
                        callLogLegList.add(callLogLeg);
                        pojo.setLegList(callLogLegList);
                        callLogPojoList.add(pojo);
                    }
                }
            }
        }
        return callLogPojoList;
    }

    /**
     * @param callHistoryCriteria object of CallHistoryCriteria.
     * <p>
     * pageNumber: page number [0,1,2,...] <br/>
     * pageSize: number of records per page and it should be grater than 0 <br/>
     * fromDate: start date should be in timestamp format and its numeric (no
     * millisecond: 10 digits) fromDate <= toDate  <br/> toDate: end date should
     * be in timestamp format and its numeric (no millisecond: 10 digits) <br/>
     * callType: its call_direction [null,0,1,2] <br/>
     * accountPk: loan Id: [null,1,2,3,4,...] <br/>
     * </p>
     * <p>
     * JASON Sample: <br/>
     * {
     * "fromDate" : 1512086400, "toDate" : 1514678400, "callerPhoneNumber" : "",
     * "calleePhoneNumber" : "", "callType" : null, "accountPk" : null,
     * "dialerCall" : null, "userDisposition" : null, "pageNumber" : 0,
     * "pageSize" : 100 } </p>
     * @return CallDetailRecordResult contains total records number and List of
     * CallDetailRecord
     * @throws RuntimeException
     */
    public CallDetailRecordResult getCallLogLegs(CallHistoryCriteria callHistoryCriteria) throws RuntimeException {

        if (callHistoryCriteria == null
                || callHistoryCriteria.getFromDate() == 0
                || callHistoryCriteria.getToDate() == 0
                || callHistoryCriteria.getPageSize() == 0) {
            throw new RuntimeException("mandatory fields are missing");
        }

        if (callHistoryCriteria.getFromDate() > callHistoryCriteria.getToDate()) {
            throw new RuntimeException("invalid input dates");
        }

        //LOG.info("From:" + callHistoryCriteria.getFromDate());
        //LOG.info("To:" + callHistoryCriteria.getToDate());
        CallDetailRecordResult callDetailRecordResult = new CallDetailRecordResult();

        //building WHERE statement
        String whereString = " created_time BETWEEN ?1 AND ?2 ";
        if (callHistoryCriteria.getAccountPk() != null && callHistoryCriteria.getAccountPk() > 0) {
            whereString += " AND cdr.account_pk = " + callHistoryCriteria.getAccountPk();
        }
        if (callHistoryCriteria.getCallType() != null && callHistoryCriteria.getCallType() > -1) {
            if (callHistoryCriteria.getCallType() == 0) {
                whereString += " AND cdr.call_direction = 'INTERNAL'";
            } else if (callHistoryCriteria.getCallType() == 1) {
                whereString += " AND cdr.call_direction = 'INBOUND'";
            } else {
                whereString += " AND cdr.call_direction = 'OUTBOUND'";
            }
        }
        if (callHistoryCriteria.getCallerPhoneNumber() != null && !"".equals(callHistoryCriteria.getCallerPhoneNumber().replaceAll("[^\\d.]", ""))) {
            whereString += " AND cdr.caller_id_number = '" + callHistoryCriteria.getCallerPhoneNumber().replaceAll("[^\\d.]", "") + "'";
        }
        if (callHistoryCriteria.getCalleePhoneNumber() != null && !"".equals(callHistoryCriteria.getCalleePhoneNumber().replaceAll("[^\\d.]", ""))) {
            whereString += " AND cdr.callee_id_number = '" + callHistoryCriteria.getCalleePhoneNumber().replaceAll("[^\\d.]", "") + "'";
        }
        if (callHistoryCriteria.getDialerCall() != null) {
            whereString += " AND cdr.dialer = " + callHistoryCriteria.getDialerCall();
        }
        if (callHistoryCriteria.getUserDisposition() > 0) {
            whereString += " AND cdr.user_disposition = " + callHistoryCriteria.getUserDisposition();
        }

        //final long queryCount = ((Number)entityManager.createNativeQuery("SELECT count(1) FROM sti.sti_call_detail_record cdr WHERE " + whereString).getSingleResult()).longValue();
        final Query queryCount = entityManager.createNativeQuery("SELECT count(cdr.pk) FROM sti.sti_call_detail_record cdr WHERE " + whereString);
        queryCount.setParameter(1, new Timestamp(callHistoryCriteria.getFromDate() * 1000));
        queryCount.setParameter(2, new Timestamp(callHistoryCriteria.getToDate() * 1000));
        callDetailRecordResult.setTotalRecordCount(((Number) queryCount.getSingleResult()).longValue());

        //LOG.info("callDetailRecordResult.getTotalRecordCount():" + callDetailRecordResult.getTotalRecordCount());
        if (callDetailRecordResult.getTotalRecordCount() > 0) {

            long queryExTime = System.currentTimeMillis();

            final Query queryResult = entityManager.createNativeQuery(
                    "SELECT cdr.* , "
                    + " FALSE AS has_sub_calls, " + //hasSubCalls
                    " '' AS sub_calls " + //subCalls
                    " FROM sti.sti_call_detail_record cdr "
                    + " WHERE " + whereString
                    + " ORDER BY cdr.pk OFFSET ?3 LIMIT ?4 ", CallDetailRecord.class);

            queryResult.setParameter(1, new Timestamp(callHistoryCriteria.getFromDate() * 1000));
            queryResult.setParameter(2, new Timestamp(callHistoryCriteria.getToDate() * 1000));
            queryResult.setParameter(3, callHistoryCriteria.getPageNumber() * callHistoryCriteria.getPageSize());
            queryResult.setParameter(4, callHistoryCriteria.getPageSize());

            callDetailRecordResult.setCallDetailRecordList(queryResult.getResultList());

            LOG.info("queryExTime:" + (System.currentTimeMillis() - queryExTime));
        }
        //entityManager.flush();
        //entityManager.clear();
        //entityManager.close();

        return callDetailRecordResult;
    }

    private String getTMSCallDetailsQuery() {
        return "SELECT new " + TMSCallDetails.class.getName()
                + "(account.pk as accountPk, "
                + "customers.personalInfo.firstName as firstName, "
                + "customers.personalInfo.lastName as lastName, "
                + "customers.personalInfo.ssn as ssn, "
                + "address1 as address1, "
                + "address2 as address2, "
                + "city as city, "
                + "state as state, "
                + "zip5 as zip, "
                + "doNotCall as doNotCall,"
                + "callerId as callerId,"
                //                + "loan.pendingBK as pendingBK, "
                //                + "case when loanStatus.chargeOffDate is not null then true else false end as isChargedOff, "
                //                + "loanStatus.loanStatus as loanStatus, "
                //                + "loanStatus.loanServicingStatus as loanServicingStatus, "
                //+ "coalesce(loan.svLoanCollectionQueue.primaryAgent.phoneExtension) as defaultExtension,"
                + "dialerQueuePk as dialerQueuePk,"
                + "autoAnswerEnabled as autoAnswerEnabled, "
                + "popupDisplayMode as popupDisplayMode, "
                + "disableSecondaryAgentsCallRouting as disableSecondaryAgentsCallRouting ";
//                + "queue as svCollectionQueue, "
//                + "queue.colQueueData.queueName as queueName, "
//                + "queue.colQueueData.portfolioType as portfolioType, "
//                + "ach.achInformation.autoPaymentOption as achAutoPaymentStatus)";
    }

    public TMSCallDetails getLoanInfoByLoanPk(long loanPk) {
        TypedQuery<TMSCallDetails> q = entityManager.createQuery(getTMSCallDetailsQuery()
                + " FROM Account account "
                + " join account.customers customer "
                + " join customer.phones phone "
                //                + " join account.svLoanStatus loanStatus "
                + " left outer join customer.address addr"
                + " left outer join account.inboundDialerQueue.svDialerQueueSettings settings"
                //                + " left outer join account.svLoanCollectionQueue queue"
                //                + " left outer join account.svAchInfoForLoan ach"
                + " WHERE account.pk = :accountPk"
                + " ORDER BY account.class, phone.phoneType", TMSCallDetails.class);
        q.setParameter("loanPk", loanPk);
        q.setMaxResults(1);
        return getTMSCallDetails(q);
    }

    // TBD
    public TMSCallDetails getLoanInfoByPhoneNumber(Long phoneNumber) {
        NationalPhoneNumber phone = PhoneUtils.parsePhoneNumber(phoneNumber);
        if (phone == null) {
            return null;
        }
        String sql = getTMSCallDetailsQuery();
//        boolean includeSkip = false;
//        if (config.getBoolean("getLoanInfoByPhoneNumber.includeSkip", Boolean.FALSE)){
//            includeSkip = true;
//            sql = sql.replace("phone.doNotCall", "coalesce(phone.doNotCall, skipRef.refInfo.skipRefPhoneDoNotUse)");
//        }
//        String finalSql = sql;
//        if(includeSkip){
//            finalSql = sql+getLoanInfoByPhoneNumberQueryWithSkip();
//        }else{
//            finalSql = sql+getLoanInfoByPhoneNumberQueryWithoutSkip();
//        }
        TypedQuery<TMSCallDetails> q = entityManager.createQuery(sql, TMSCallDetails.class);
//        q.setParameter("areaCode", phone.getAreaCode());
//        q.setParameter("phoneNumber", phone.getLocalNumber());
//        if (finalSql.contains(":fullPhone")){
//            q.setParameter("fullPhone", phoneNumber);
//        }
        return getTMSCallDetails(q);
    }

    public String getLoanInfoByPhoneNumberQueryWithSkip() {
        return " FROM SvLoan loan "
                + " join loan.svBorrowers borrower"
                + " left outer join borrower.borrowerPhone phone WITH phone.areaCode = :areaCode AND phone.phoneNumber = :phoneNumber"
                + " left outer join loan.svSkipReference skipRef WITH skipRef.refInfo.skipRefPhone = :fullPhone "
                + " join loan.svLoanStatus loanStatus "
                + " left outer join borrower.borrowerCurrentAddress addr"
                + " left outer join loan.svInboundDialerQueue.svDialerQueueSettings settings"
                + " left outer join loan.svLoanCollectionQueue queue"
                + " left outer join loan.svAchInfoForLoan ach "
                + " WHERE (borrower.pk = phone.svPhoneBorrower.pk or borrower member of loan.svPrimaryBorrowers)"
                + " AND ((phone.areaCode = :areaCode AND phone.phoneNumber = :phoneNumber AND phone.loanPk = loan.pk)  OR (skipRef.refInfo.skipRefPhone = :fullPhone))";
    }

    public String getLoanInfoByPhoneNumberQueryWithoutSkip() {
        return " FROM SvLoan loan "
                + " join loan.svBorrowers borrower"
                + " left outer join borrower.borrowerPhone phone WITH phone.areaCode = :areaCode AND phone.phoneNumber = :phoneNumber"
                + " join loan.svLoanStatus loanStatus "
                + " left outer join borrower.borrowerCurrentAddress addr"
                + " left outer join loan.svInboundDialerQueue.svDialerQueueSettings settings"
                + " left outer join loan.svLoanCollectionQueue queue"
                + " left outer join loan.svAchInfoForLoan ach "
                + " WHERE (borrower.pk = phone.svPhoneBorrower.pk or borrower member of loan.svPrimaryBorrowers)"
                + " AND ((phone.areaCode = :areaCode AND phone.phoneNumber = :phoneNumber AND phone.loanPk = loan.pk))";
    }

    public TMSCallDetails getTMSCallDetails(TypedQuery<TMSCallDetails> q) {
        List<TMSCallDetails> list = (List<TMSCallDetails>) q.getResultList();
        TMSCallDetails callDetails = list.isEmpty() ? new TMSCallDetails() : list.get(0);

//        if (list.size() > 1 && new HashSet<>(list).size() > 1) {
//            HashSet<String> ssnSet = new HashSet<>();
//            for (TMSCallDetails pojo : list) {
//                ssnSet.add(pojo.getSsn());
//                Integer servStatus = pojo.getLoanServicingStatus();
//                callDetails = (servStatus != LoanServicingStatus.COLL_STATUS_PAID_OFF
//                        && servStatus != LoanServicingStatus.COLL_STATUS_SETTLED_IN_FULL
//                        && servStatus != LoanServicingStatus.COLL_STATUS_SOLD_TO_COLLECTIONS) ? pojo : callDetails;
//            }
//            if (ssnSet.size() > 1) {
//                callDetails.setHasMultipleMatches(true);
//            }
//        }
        Integer defaultExtension = companyInfo.getCustomerServicePhoneExtension();
        callDetails.setDefaultExtension(defaultExtension == null ? 1001 : defaultExtension);
        if (callDetails.getDialerQueuePk() == null) {
            try {
                DialerQueueDetails dq = dqService.getDefaultInboundQueue();
                InboundDialerQueueSettings settings = dqService.getInboundDQSettingsByDQPk(dq.getPk());
                if (settings != null) {
                    callDetails.setDialerQueuePk(dq.getPk());
                    callDetails.setAutoAnswerEnabled(settings.isAutoAnswerEnabled());
                    callDetails.setPopupDisplayMode(settings.getPopupDisplayMode());
                }
            } catch (StiException ex) {
                LOG.error("Error getting default inbound queue: ", ex);
            }
        }
//        SvCollectionQueue queue = callDetails.getSvCollectionQueue();
//        if (queue != null) {
//            if (queue.getSvPrimaryAgent() != null) {
//                callDetails.setPrimaryAgentUsername(queue.getSvPrimaryAgent().getUserName());
//            }
//            for (AgentQueue agentQueue : queue.getAgentQueues()) {
//                //Removed secondaryAgents with assignedUntil before today's date
//                if (agentQueue.getSvSecondaryAgent() != null && (agentQueue.getAssignedUntil() == null || agentQueue.getAssignedUntil().isAfter(LocalDate.now()))
//                        && agentQueue.getAllowCalls() != null && agentQueue.getAllowCalls()) {
//                    callDetails.getSecondaryAgentUsernameList().add(agentQueue.getSvSecondaryAgent().getUserName());
//                }
//            }
//        }

        return callDetails;
    }

    @SuppressWarnings("unchecked")
    public List<PhoneNumberAccountData> getAccountsForPhoneNumber(Long phoneNumber) {
        List<PhoneNumberAccountData> retList = new ArrayList<>();
        Integer startTime = LocalDateTime.now().getMillisOfDay();
        NationalPhoneNumber phone = PhoneUtils.parsePhoneNumber(phoneNumber);
        if (phone == null) {
            return null;
        }
//        Boolean useNativeQuery = config.getBoolean("tmsService.getLoansForPhoneNumber.useNativeQuery", Boolean.TRUE);
//        if(!useNativeQuery){
//            TypedQuery<PhoneNumberAccountData> q = entityManager.createQuery(getAccountsForPhoneNumberQueryWithSkip(), PhoneNumberAccountData.class);
//            q.setParameter("areaCode", phone.getAreaCode());
//            q.setParameter("phoneNumber", phone.getLocalNumber());
//            q.setParameter("fullPhone", phoneNumber);
//            retList = new ArrayList<>(new HashSet<>(q.getResultList()));
//        }else{
//            String query = sqlConfigRepo.getQueryBySqlName("GetLoansForPhoneNumber");
//            Session session = entityManager.unwrap(Session.class);
//            org.hibernate.Query q = session.createSQLQuery(query);
//            q.setParameter("areaCode", phone.getAreaCode());
//            q.setParameter("phoneNumber", phone.getLocalNumber());
//            q.setParameter("fullPhone", phoneNumber);
//            q.setResultTransformer(Transformers.aliasToBean(PhoneNumberAccountData.class));
//            retList = q.list();
//        }        
        LOG.debug("getLoansForPhoneNumber for phone {} took {} msec", phoneNumber, (new LocalDateTime().getMillisOfDay() - startTime));
//        exceptionService.createTimeLogAsync("TMSService", "getLoansForPhoneNumber", new Object[]{"Given phone: "+phoneNumber, "NationalPhoneNumber: "+phone.toLog(), "useNativeQuery: "+useNativeQuery}
//                    , null, phoneNumber.toString(), Long.valueOf(new LocalDateTime().getMillisOfDay() - startTime), new Object[]{"Number of results returned : "+retList.size()});
        return retList;
    }

}
