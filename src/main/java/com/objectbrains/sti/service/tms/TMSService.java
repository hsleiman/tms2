/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.tms;

import com.objectbrains.config.CompanyInfo;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.sti.constants.DialPlanContext;
import com.objectbrains.sti.db.entity.agent.AgentDialerGroup;
import com.objectbrains.sti.db.entity.agent.AgentQueue;
import com.objectbrains.sti.db.entity.agent.DialerGroup;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.dialer.BIMessage;
import com.objectbrains.sti.db.entity.base.dialer.CallDetailRecord;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueue;
import com.objectbrains.sti.db.entity.base.dialer.DialerQueueGroupAssociation;
import com.objectbrains.sti.db.entity.base.dialer.InboundDialerQueueSettings;
import com.objectbrains.sti.db.entity.base.dialer.SpeechToText;
import com.objectbrains.sti.db.entity.base.dialer.StiCallerId;
import com.objectbrains.sti.db.entity.disposition.CallDispositionCode;
import com.objectbrains.sti.db.repository.AgentRepository;
import com.objectbrains.sti.db.repository.account.AccountRepository;
import com.objectbrains.sti.db.repository.dialer.BIMessageRepository;
import com.objectbrains.sti.db.repository.dialer.CallDetailRecordRepository;
import com.objectbrains.sti.db.repository.dialer.DialerQueueRepository;
import com.objectbrains.sti.db.repository.disposition.CallDispositionRepository;
import com.objectbrains.sti.db.repository.qaform.CallQualityManagementRepository;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import com.objectbrains.sti.embeddable.BIPlaybackData;
import com.objectbrains.sti.embeddable.DialerQueueDetails;
import com.objectbrains.sti.embeddable.InboundDialerQueueRecord;
import com.objectbrains.sti.embeddable.WeightedPriority;
import com.objectbrains.sti.exception.StiException;
import com.objectbrains.sti.pojo.*;
import com.objectbrains.sti.service.dialer.DialerQueueService;
import com.objectbrains.sti.service.dialer.PhoneNumberCallable;
import com.objectbrains.sti.service.utility.DurationUtils;
import com.objectbrains.sti.service.utility.PhoneUtils;
import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.joda.time.LocalDate;

/**
 * @author David
 */
@Service
@Transactional
public class TMSService {

    public static final Logger LOG = LoggerFactory.getLogger(TMSService.class);
    @ConfigContext
    private ConfigurationUtility config;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private CallDetailRecordRepository cdrRepo;
    @Autowired
    private CallQualityManagementRepository callQualityRepo;
    @Autowired
    private DialerQueueRepository dqRepo;
    @Autowired
    private AgentRepository agentRepo;
    @Autowired
    private DialerQueueService dqService;
    @PersistenceContext
    private EntityManager entityManager;
    @ConfigContext
    private CompanyInfo companyInfo;
    @Autowired
    private BIMessageRepository biMessageRepository;
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
        return callDurationInMSeconds > config.getDouble("successful.call.duration.threshold", 10000d);
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

    public void saveBIMessage(BIMessage message) {
        biMessageRepository.persist(message);
    }

    public BIPlaybackData getBIPlaybackData(String callUUID) {
        if (StringUtils.isNotBlank(callUUID)) {
            callUUID = callUUID.trim();
            CallDetailRecord record = cdrRepo.locateCallDetailRecordByCallUUID(callUUID);
            BIPlaybackData data = new BIPlaybackData();
            if (record != null && record.getEndTime() != null) {
                data.setCallLength(DurationUtils.getDuration(record.getStartTime(), record.getEndTime()));
                data.setPlaybackElements(biMessageRepository.getPlaybackElements(record));
                data.setAudioUrl(record.getCallRecordingUrl());
            }
            return data;
        }
        return null;
    }

    public List<DialerQueueDetails> getAllDialerQueues() {
        return getDialerQueueDetailsList(dqRepo.getAllDialerQueues());
    }

    private List<DialerQueueDetails> getDialerQueueDetailsList(List<DialerQueue> dialerQueueList) {
        List<DialerQueueDetails> dqDetails = new ArrayList<>();
        for (DialerQueue dq : dialerQueueList) {
            dq.getDialerQueueDetails().setPk(dq.getPk());
            dqDetails.add(dq.getDialerQueueDetails());
            DialerQueueGroupAssociation dialerQueueGroupAssociation = dqRepo.getQueueGroupAssociationByDialerQueue(dq);
            if (dialerQueueGroupAssociation != null) {
                if (dialerQueueGroupAssociation.getSecondaryGroup() != null) {
                    dq.getDialerQueueDetails().setSecondaryGroupPk(dialerQueueGroupAssociation.getSecondaryGroup().getPk());
                }
            }
        }
        return dqDetails;
    }

    public DialerQueueDetails getDialerQueueByPk(long dqPk) throws StiException {
        DialerQueue dq = dqRepo.locateDialerQueueByPk(dqPk);
        dq.getDialerQueueDetails().setPk(dq.getPk());
        return dq.getDialerQueueDetails();
    }

    // serivce exist in outbounddialerservice
    public OutboundDialerQueueRecord getOutboundDialerQueueRecord(long dqPk) throws StiException {
//        if (isLegacyCodeOn()) {
//            return getRecordViaPlanA(dqPk);
//        }
//        if (isOptimizedWayOn()) {
//            return getRecordViaOptimizedWayPlanB(dqPk);
//        }
//        if (isHazelcastOn()) {
//            return getRecordViaHazelcastPlanD(dqPk);
//        }
        return null;
    }

    public List<QueueAgentWeightPriority> getQueueAgentWeightPriorityForUsername(String username) {
        List<QueueAgentWeightPriority> qawpList = new ArrayList<>();
        long agentPk = agentRepo.locateByAgentUserName(username).getPk();
        List<AgentDialerGroup> results = entityManager.createNamedQuery("AgentDialerGroup.LocateByAgentPk", AgentDialerGroup.class).
                setParameter("agentPk", agentPk).getResultList();
        for (AgentDialerGroup adg : results) {
            List<DialerQueueGroupAssociation> secondaryList = new ArrayList<>();
            List<DialerQueueGroupAssociation> primaryDqAssocList = dqRepo.getQueueGroupAssociationByDialerGroup(adg.getDialerGroup());
            List<DialerQueueGroupAssociation> secondaryDqAssocList = dqRepo.getQueueGroupAssociationBySecondaryDialerGroup(adg.getDialerGroup());

            for (DialerQueueGroupAssociation svdqa : primaryDqAssocList) {
                QueueAgentWeightPriority queueAgentWeightPriority = new QueueAgentWeightPriority(adg.getWeightedPriority(),
                        svdqa.getDialerQueue().getPk(), adg.isLeader(), adg.getAllowAfterHours(), svdqa.getDialerGroup().getPk(), true);
                qawpList.add(queueAgentWeightPriority);
            }

            for (DialerQueueGroupAssociation secondary : secondaryDqAssocList) {
                boolean isInList = false;
                for (DialerQueueGroupAssociation assoc : primaryDqAssocList) {
                    if (secondary.getDialerQueue().getPk() == assoc.getDialerQueue().getPk()) {
                        isInList = true;
                    }
                }
                if (!isInList) {
                    secondaryList.add(secondary);
                }
            }

            for (DialerQueueGroupAssociation svdqa : secondaryList) {
                QueueAgentWeightPriority queueAgentWeightPriority = new QueueAgentWeightPriority(adg.getWeightedPriority(),
                        svdqa.getDialerQueue().getPk(), adg.isLeader(), adg.getAllowAfterHours(), svdqa.getSecondaryGroup().getPk(), false);
                qawpList.add(queueAgentWeightPriority);
            }

        }

        return qawpList;
    }

    public List<AgentWeightPriority> getAgentWeightPriorityListForGroup(long dialerGroupPk) throws StiException {
        DialerGroup dg = agentRepo.locateByDialerGroupPk(dialerGroupPk);
        return dqService.getAgentWeightPriorityForDialerGroup(dg);
    }

    public InboundDialerQueueRecord getInboundDialerQueueRecord(long dqPk) throws StiException {
        InboundDialerQueueRecord dqRecord = new InboundDialerQueueRecord(dqPk);
        //instantiateDialerQueueRecord(dqPk, dqRecord);
        return dqRecord;
    }

    public CallDispositionCode getCallDispositionCode(long dispositionId) {
        CallDispositionCode code = dispositionRepository.locateDispositionById(dispositionId);
        if (code == null) {
            throw new EntityNotFoundException("CallDispositionCode with dispositionId [" + dispositionId + "] could not be found");
        }
        return code;
    }

    public CallDispositionCode getCallDispositionCodeByQCode(int qCode) {
        return dispositionRepository.locateDispositionByQCode(qCode);
    }
    
     public CallDispositionCode getCallDispositionCodeByDispositionName(String disposition) {
        CallDispositionCode code = dispositionRepository.locateDisposition(disposition);
        if (code == null) {
            throw new EntityNotFoundException("CallDispositionCode with disposition [" + disposition + "] could not be found");
        }
        return code;
    }
     
      public List<StiCallerId> getAllCallerIds() {
        return dqRepo.getAllCallerIds();
    }
      
      public List<AgentWeightPriority> getAgentWeightPriorityListForDq(long dqPk) throws StiException {
        DialerGroup primaryGroup = getPrimaryDialerGroupForDQ(dqPk);
        DialerGroup secondaryGroup = getSecondaryDialerGroupForDQ(dqPk);
        return getAgentWeightPriorityListForGroups(primaryGroup, secondaryGroup);
    }
      
      private List<AgentWeightPriority> getAgentWeightPriorityListForGroups(DialerGroup primaryGroup, DialerGroup secondaryGroup) {
        List<AgentWeightPriority> agentWeightPriorityList = new ArrayList<>();
        for (AgentDialerGroup agentDialerGroup : primaryGroup.getDialerGroupAgents()) {
            agentWeightPriorityList.add(setAgentWeightPriorityForGroup(agentDialerGroup, true));
        }
        if (secondaryGroup != null) {
            for (AgentDialerGroup agentDialerGroup : secondaryGroup.getDialerGroupAgents()) {
                boolean isAgentInList = false;
                for (AgentWeightPriority awp : agentWeightPriorityList) {
                    if (agentDialerGroup.getDialerAgent().getUserName().equals(awp.getUsername())) {
                        isAgentInList = true;
                    }
                }
                if (!isAgentInList) {
                    agentWeightPriorityList.add(setAgentWeightPriorityForGroup(agentDialerGroup, false));
                }
            }
        }
        return agentWeightPriorityList;
    }
      
      private AgentWeightPriority setAgentWeightPriorityForGroup(AgentDialerGroup agentDialerGroup, boolean isPrimaryGroup) {
        AgentWeightPriority agentWeightPriority = new AgentWeightPriority();
        agentWeightPriority.setUsername(agentDialerGroup.getDialerAgent().getUserName());
        agentWeightPriority.setFirstName(agentDialerGroup.getDialerAgent().getFirstName());
        agentWeightPriority.setLastName(agentDialerGroup.getDialerAgent().getLastName());
        agentWeightPriority.setLeader(agentDialerGroup.isLeader());
        agentWeightPriority.setGroupPk(agentDialerGroup.getDialerGroup().getPk());
        agentWeightPriority.setIsPrimaryGroup(isPrimaryGroup);
        if (agentDialerGroup.getAllowAfterHours() != null) {
            agentWeightPriority.setAllowAfterHours(agentDialerGroup.getAllowAfterHours());
        } else {
            agentWeightPriority.setAllowAfterHours(Boolean.FALSE);
        }
        WeightedPriority weightedPriority = agentDialerGroup.getWeightedPriority();
        setDefaultWeightPriority(weightedPriority);
        agentWeightPriority.setWeightedPriority(weightedPriority);
        return agentWeightPriority;
    }
      
       private void setDefaultWeightPriority(WeightedPriority weightedPriority) {
        if (weightedPriority.getPriority() == null) {
            weightedPriority.setPriority(5);
        }
        if (weightedPriority.getWeight() == null) {
            weightedPriority.setWeight(5);
        }
    }
       
       public DialerGroup getPrimaryDialerGroupForDQ(long queuePk) throws StiException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        if (assoc != null) {
            return agentRepo.locateByDialerGroupPk(assoc.getDialerGroup().getPk());
        }
        return null;
    }

    public DialerGroup getSecondaryDialerGroupForDQ(long queuePk) throws StiException {
        DialerQueue queue = dqRepo.locateDialerQueueByPk(queuePk);
        DialerQueueGroupAssociation assoc = dqRepo.getQueueGroupAssociationByDialerQueue(queue);
        if (assoc != null) {
            if (assoc.getSecondaryGroup() != null) {
                return agentRepo.locateByDialerGroupPk(assoc.getSecondaryGroup().getPk());
            }
        }
        return null;
    }
    
     public PhoneNumberCallable canCallNumberInQueue(long dqPk, long loanPk, long phoneNumber) {
//        int startCanCall = new LocalDateTime().getMillisOfDay();
//        LOG.debug("canCallNumber start for dqPk: {} loanPk: {} and phoneNumber: {}", dqPk, loanPk, phoneNumber);
//        SvDialerQueue queue = dqRepo.locateDialerQueueByPk(dqPk);
//        SvLoan svl = loanRepo.locateByLoanPk(loanPk);
//        boolean isLoanInQueue = isLoanInQueue(queue, svl);
//        if (!isLoanInQueue) {
//            throw new LoanNotInQueueException(loanPk, dqPk);
//        }
//        String sql = queue.getDialerQueueDetails().getSqlQuery();
//        String tableName = " sv_loan ";
//        if (sql.contains("svc.sv_loan")) {
//            tableName = " svc.sv_loan ";
//        }
//        sql = sql.replace(tableName, " svc.sv_loan join (select * from svc.sv_loan where pk = " + loanPk + ") loan2 on svc.sv_loan.pk = loan2.pk ");
//        LOG.debug("canCallNumberInQueue : sql to be executed: {}", sql);
//        List<Long> n = dqRepo.executeSqlScript(sql);
//        LOG.debug("Ended sql execution .. Took {} msec", (new LocalDateTime().getMillisOfDay() - startCanCall));
//        if (n.isEmpty()) {
//            LOG.info("canCallNumberInQueue {} for loan {} is not in queue {} anymore.", phoneNumber, loanPk, dqPk);
//            String note = "Loan " + loanPk + " with PhoneNumber " + phoneNumber + " is not in the dialer queue " + queue.getDialerQueueDetails().getQueueName() + " anymore";
//            collectionLogRepo.createCollectionLog(svl, note, CollectionLogTypes.COLLECTION_LOG_DIALER, null, false);
//            throw new LoanNotInQueueException(loanPk, dqPk);
//        }
//        if (config.getBoolean("can.call.number.check.reviwed.time", Boolean.TRUE) && ((svl.getLastReviewedDateTime() != null
//                && svl.getLastReviewedDateTime().isAfter(new LocalDateTime().minusHours(config.getInteger("can.call.number.hours.check", 6)))) || svl.getLastReviewedDateTime() == null)) {
//            if (svl.isPendingBK() != null && svl.isPendingBK()) {
//                LOG.info("LoanPk {} has pendingBk ", loanPk);
//                String note = "LoanPk " + loanPk + " has pendingBk";
//                collectionLogRepo.createCollectionLog(svl, note, CollectionLogTypes.COLLECTION_LOG_DIALER, null, false);
//                throw new SvcException("LoanPk " + loanPk + " has pendingBk or verbal c&d set");
//            }
//            boolean isVerbalCD = false;
//            if (svl.isVerbalCeaseAndDesist() != null && svl.isVerbalCeaseAndDesist()) {
//                isVerbalCD = true;
//            }
//            List<SvPhone> svPhones = new ArrayList<>();
//            if (config.getBoolean("can.call.number.check.phones.new.way", Boolean.TRUE)) {
//                LocalDateTime start = new LocalDateTime();
//                LOG.info("Get phones new way at {}", start);
//                for (SvBorrower svBwr : svl.getSvBorrowers()) {
//                    for (SvPhone svPhone : svBwr.getBorrowerPhone()) {
//                        if (svPhone.getAreaCode() * 10000000 + svPhone.getPhoneNumber() == phoneNumber) {
//                            checkForDncAndVerbalCandD(svPhone, phoneNumber, isVerbalCD, svl);
//                        }
//                    }
//                }
//                LOG.warn("Get phones for loan {} and phone number {} new way took{} ms", svl, phoneNumber, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
//            } else {
//                LocalDateTime start = new LocalDateTime();
//                LOG.info("Get phones old way at {}", start);
//                svPhones = bwrRepo.locateSvPhoneByPhoneNumberAndLoan(Long.toString(phoneNumber), loanPk);
//                if (svPhones != null && !svPhones.isEmpty()) {
//                    for (SvPhone svPhone : svPhones) {
//                        checkForDncAndVerbalCandD(svPhone, phoneNumber, isVerbalCD, svl);
//                    }
//                }
//                LOG.warn("Get phones query for loan {} and phone number {} old way took :{} ms", svl, phoneNumber, (new LocalDateTime().getMillisOfDay() - start.getMillisOfDay()));
//            }
//        }
//
//        LOG.debug("Dialer queue and phone check done. Calculating best time to call...");
//        NationalPhoneNumber phone = PhoneUtils.parsePhoneNumber(phoneNumber);
//        TypedQuery<String> q = entityManager.createQuery("SELECT addr.zip5 as zipCode"
//                + " FROM SvLoan loan "
//                + " join loan.svBorrowers bwr "
//                + " join loan.svAddresses addr "
//                + " join bwr.borrowerPhone phone "
//                + " WHERE loan.pk = :loanPk"
//                + " AND addr.svBorrower = bwr"
//                + " AND phone.svPhoneBorrower = bwr"
//                + " AND phone.areaCode = :areaCode"
//                + " AND phone.phoneNumber = :phoneNumber"
//                + " ORDER BY bwr.class, addr.class", String.class);
//        q.setParameter("loanPk", loanPk);
//        q.setParameter("areaCode", phone.getAreaCode());
//        q.setParameter("phoneNumber", phone.getLocalNumber());
//        q.setMaxResults(1);
//        List<String> list = (List<String>) q.getResultList();
//        String zipCode = null;
//        if (!list.isEmpty()) {
//            zipCode = (String) list.get(0);
//        }
        PhoneNumberCallable phCallable = null; //= dqRepo.getPhoneNumberCallable(zipCode, phone.getAreaCode());
        LOG.info("canCallNumberInQueue {} calltimeCode :  {}", phoneNumber, phCallable.getCallTimeCode());
//        if (phCallable.getCallTimeCode() != CallTimeCode.OK_TO_CALL) {
//            String note = "PhoneNumber " + phoneNumber + " is not callable (" + phCallable.getCallTimeCode() + ") for dialer " + queue.getDialerQueueDetails().getQueueName();
//            collectionLogRepo.createCollectionLog(svl, note, CollectionLogTypes.COLLECTION_LOG_DIALER, null, false);
//        }
//        LOG.debug("canCallNumberInQueue ended. Took {} msec", (new LocalDateTime().getMillisOfDay() - startCanCall));
        return phCallable;
    }
     
    public List<AccountCustomerName> getBasicLoanDataForQueue(long queuePk, Integer pageNum, Integer pageSize) {
        List<AccountCustomerName> list = new ArrayList<>();
       
        return list;
    } 
}
