/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository;

import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.entity.agent.AgentQueue;
import com.objectbrains.sti.db.entity.agent.DialerGroup;
import com.objectbrains.sti.db.entity.agent.Team;
import com.objectbrains.sti.db.repository.utility.SQLConfigRepository;
import com.objectbrains.sti.exception.TooManyObjectFoundException;
import com.objectbrains.sti.ows.AccountManagerOWS;
import com.objectbrains.sti.ows.AmsUser;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author David
 */
@Repository
public class AgentRepository {

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    private CollectionQueueRepository colQRepo;
    @Autowired
    private AccountManagerOWS accountManagerOWS;

    @Autowired
    private SQLConfigRepository sqlConfigRepo;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AgentRepository.class);

    EntityManager getEntityManager() {
        return entityManager;
    }
    
    public Agent getAgentByPk(long pk) {
        return entityManager.find(Agent.class, pk);
    }
    

    public List<Agent> getAllAgents() {
        return getEntityManager().createNamedQuery("Agent.getAllAgents", Agent.class).getResultList();

    }

    public List<Agent> getAgentsForManager(String managerUserName) {
        return getEntityManager().createNamedQuery("Agent.getAllAgentsForManager", Agent.class).setParameter("manager1", managerUserName).getResultList();

    }

    public Agent locateByAgentPk(long agentPk) {
        return getEntityManager().createNamedQuery("Agent.LocateByPk", Agent.class).
                setParameter("pk", agentPk).getSingleResult();
    }

    public Team locateByTeamPk(long teamPk) {
        List<Team> res = getEntityManager().createNamedQuery("Team.LocateByPk", Team.class).
                setParameter("pk", teamPk).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many entries for a given pk");
        }
        return res.get(0);
    }

    public Agent locateByAgentUserName(String userName) {
        List<Agent> res = getEntityManager().createNamedQuery("Agent.locateByAgentUserName", Agent.class).
                setParameter("userName", userName.toLowerCase()).getResultList();
        if ((res == null) || (res.isEmpty())) {
            //If no agent found in svc but is in ams, create one from ams. Else, return null.
            Agent Agent = new Agent();
            Agent.setUserName(userName);
            Agent retAgent = syncAgentWithUser(Agent);
            if (retAgent == null) {
                return null;
            } else {
                entityManager.persist(retAgent);
                return retAgent;
            }
        }else{
            syncAgentWithUser(res.get(0));
        }
        return res.get(0);
    }

    public List<Agent> locateByAgentPhoneExtension(Long phoneExtension) {
        List<Agent> res = getEntityManager().createNamedQuery("Agent.locateByAgentPhoneExtension", Agent.class).
                setParameter("phoneExtension", phoneExtension).getResultList();
        return res;
    }

    public Team locateByTeamName(String teamName) {
        List<Team> res = getEntityManager().createNamedQuery("team.locateByTeamName", Team.class).
                setParameter("teamName", teamName).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many entries for a given teamName");
        }
        return res.get(0);
    }

    public List<AgentQueue> locateAgentQueueByAgentPk(long agentPk) {
        List<AgentQueue> res = getEntityManager().createNamedQuery("AgentQueue.LocateByAgentPk", AgentQueue.class).
                setParameter("agentPk", agentPk).getResultList();
        return res;
    }

    public List<AgentQueue> locateAgentQueueByQueuePk(long queuePk) {
        List<AgentQueue> res = getEntityManager().createNamedQuery("AgentQueue.LocateByQueuePk", AgentQueue.class).
                setParameter("queuePk", queuePk).getResultList();
        return res;
    }

    public AgentQueue locateAgentQueueByAgentPkAndQueuePk(long agentPk, long queuePk) {
        TypedQuery<AgentQueue> query = getEntityManager().createNamedQuery("AgentQueue.LocateByAgentPkAndQueuePk", AgentQueue.class);
        query.setParameter("agentPk", agentPk);
        query.setParameter("queuePk", queuePk);
        List<AgentQueue> res = query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many entries for a given agentPk [" + agentPk + "] and queuePk [" + "]");
        }
        return res.get(0);
    }

    public DialerGroup locateByDialerGroupName(String groupName) {
        List<DialerGroup> res = getEntityManager().createNamedQuery("DialerGroup.locateByDialerGroupName", DialerGroup.class).
                setParameter("groupName", groupName).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many entries for a given groupName");
        }
        return res.get(0);

    }

    public DialerGroup locateByDialerGroupPk(long dialerGroupPk) {
        return getEntityManager().createNamedQuery("DialerGroup.LocateByPk", DialerGroup.class).
                setParameter("pk", dialerGroupPk).getSingleResult();

    }

    public List<DialerGroup> getAllDialerGroups() {
        return getEntityManager().createNamedQuery("DialerGroup.LocateAll", DialerGroup.class).getResultList();
    }
//    public List<SvLoan> locateSvLoanListByAgentPkAndSortNumber(long agentPk, int sortNumber) {
//        Query query = getEntityManager().createQuery(
//                "SELECT s FROM SvLoan s WHERE "
//                        + "s.svLoanCollectionQueue.pk = :agentPk AND "
//                        + "s.sortNumberInQueue > :sortNumber"
//                        + " ORDER BY s.sortNumberInQueue ASC");
//        query.setParameter("agentPk", agentPk);
//        query.setParameter("sortNumber", sortNumber);
//        @SuppressWarnings("unchecked")
//        List<SvLoan> res = query.getResultList();
//        return res;
//    }
//    
//    public SvLoan locateSvLoanByqueuePkAndSortNumber(long queuePk, int sortNumber) {
//        Query query = getEntityManager().createQuery(
//                "SELECT s FROM SvLoan s WHERE "
//                        + "s.svLoanCollectionQueue.pk = :queuePk AND "
//                        + "s.sortNumberInQueue = :sortNumber "
//                        );
//        query.setParameter("queuePk", queuePk);
//        query.setParameter("sortNumber", sortNumber);
//        @SuppressWarnings("unchecked")
//        List<SvLoan> res = query.getResultList();
//        if(res == null || res.isEmpty()){
//            return null;
//        }
//        if(res.size() > 1){
//            throw new TooManyObjectFoundException("Too Many Loans found with the same queuePk "+queuePk+" and sortNumber : "+sortNumber);
//        }
//        return res.get(0);
//    }

    public int getMaxSortNumberInQueue(long queuePk) {
        Query query = getEntityManager().createQuery(
                "SELECT max(s.sortNumberInQueue)FROM SvLoan s WHERE "
                + "s.accountWorkQueue.pk = :queuePk ");
        query.setParameter("queuePk", queuePk);
        return (int) query.getResultList().get(0);

    }

//    @SuppressWarnings("unchecked")
//    public List<LoansInQueuePojo> getLoansInQueue(Long queuePk, Integer pageNum, Integer pageSize) {
//        
//        //Long bankruptcyPortfolio = ((Integer)WorkPortfolioType.COLL_PORTFOLIO_BANKRUPTCY).longValue();
//        //Long legalPortfolio = ((Integer)WorkPortfolioType.COLL_PORTFOLIO_LEGAL).longValue();
//        String FROMQuery = "";
//        String WHEREQuery = "";
//        WorkQueue svColQueue = colQRepo.getCollectionsQueue(queuePk);
//        Boolean isSkip = svColQueue.getColQueueData().getPortfolioType() == WorkPortfolioType.COLL_PORTFOLIO_SKIP_TRACE;
//        if (isSkip){
//            FROMQuery = " FROM WorkQueue queue "
//                    + " JOIN queue.svSkipTraceSet skipTraces "
//                + " JOIN skipTraces.svLoan loan "
//                + " JOIN loan.svPrimaryBorrowers bwr "
//                + " LEFT OUTER JOIN bwr.borrowerCurrentAddress addr "
//                + " LEFT OUTER JOIN loan.svRFDHistory rfd "
//                + " , SvLoanTerm term ";
//              //  + " LEFT OUTER JOIN term.svDueAmounts dueAmount ";
//        }else{
//            FROMQuery = " FROM WorkQueue queue "
//                + " , SvLoan loan "
//                + " JOIN loan.svPrimaryBorrowers bwr "
//                + " LEFT OUTER JOIN bwr.borrowerCurrentAddress addr "
//                + " LEFT OUTER JOIN loan.svRFDHistory rfd "
//                + " , SvLoanTerm term ";
//                //+ " LEFT OUTER JOIN term.svDueAmounts dueAmount ";
//        }
//        String queryString = "SELECT new " + LoansInQueuePojo.class.getName()+
//                "(loan.pk as loanPk,"
//                + " bwr.personalInfo.firstName as bwrFirstName,"
//                + " bwr.personalInfo.lastName as bwrLastName,"
//                + " addr.state as bwrState,"
//                + " ("
//                + " SELECT max(ptp.dueAmount.dueDate) "
//                + " FROM SvPromiseToPay ptp "
//                + " WHERE ptp.svLoanTerm = term AND "
//                + " ptp.svLoan = loan AND "
//                + " ptp.promiseToPay.ptpStatus = :pendingPtpStatus "
//                + " ) as ptpdate,"
//                + " loan.lastReviewedDateTime as reviewedDate,"
//                //+ " dueAmount.dueAmount.dueDate as nextPaymentDueDate, "
//                +" ("
//                + " select MIN(da.dueAmount.dueDate) from SvDueAmount da , SvLoanTerm term2 " 
//                + " WHERE da.svLoanTerm = term2 " 
//                + " AND da.svLoan = loan " 
//                + " AND da.dueAmount.paidAdequate = false " 
//                + " AND da.dueAmount.dueType IN (0,99) " 
//                + " AND term2.current = true"
//                + ") as nextPaymentDueDate, "
//                + " loan.lastWorkedDateTime as dateLastWorked,"
//                + " loan.myQueuelastContactTimestamp as lastContactTime, "
//                + " term.currentBalance as loanBalance, "
//                + " loan.callbackDateTime as callbackDate, "
//                + " loan.dateLastSkipWork as dateLastSkipWork,"
//                + " loan.freshStartCode as statusCodes, "
//                + " rfd.reasonForDq as reasonForDelinquency, "
//                + " (SELECT MAX(s.eftPaymentBasicData.postingDate) FROM SvAchPayment s WHERE "
//                + " s.svLoan = loan AND ( (s.achPaymentData.status = 0 OR s.achPaymentData.status = 10 OR s.achPaymentData.status = 11) AND s.achPaymentData.achStatus is NULL)) "
//                + " as pendingAchDate ,"
//                + " (SELECT MAX(s.eftPaymentBasicData.postingDate) FROM SvCreditCardPayment s WHERE s.svLoan.pk = loan.pk AND s.ccPaymentData.status = :pendingCCPaymentStatus) as pendingCCDate)";
//            //    + " CASE "
//            //    + "     WHEN pendingAchOrCreditCard"
//            //    + "     THEN ";
//                
//                /*+ " (SELECT max(achCCDate) AS pendingAchOrCCDate"
//                + " FROM ("
//                + "     ("
//                + "         SELECT max(s.eftPaymentBasicData.postingDate) as achCCDate "
//                + "         FROM SvAchPayment s "
//                + "         WHERE s.svLoan = loan AND ( (s.achPaymentData.status = 0 OR s.achPaymentData.status = 10 OR s.achPaymentData.status = 11)"
//                + "         AND s.achPaymentData.achStatus is NULL) "
//                + "     )"
//                + "     UNION ALL "
//                + "     ("
//                + "         SELECT max(ccPayment.eftPaymentBasicData.postingDate) as achCCDate "
//                + "         FROM SvCreditCardPayment ccPayment "
//                + "         WHERE (ccPayment.ccPaymentData.status = :pendingCCPaymentStatus) AND ccPayment.svLoan=loan"
//                + "     )"
//                + " ) AS a )  )";
//                //+ " AS pendingAchOrCCDate )";*/
//                
//                
//        WHEREQuery = " WHERE queue.pk = :queuePk AND ";
//        if(!isSkip){
//            WHEREQuery =  WHEREQuery+ " loan.svLoanCollectionQueue.pk = :queuePk AND ";
//        }
//        WHEREQuery = WHEREQuery + " (rfd.creationTimestamp = (SELECT max(rfdHis.creationTimestamp) FROM SvRFDHistory rfdHis WHERE rfdHis.svLoan = loan) OR (rfd is null)) AND "//INDEX(rfd) = 0 AND "
//                + " term.svLoan.pk = loan.pk AND "
//                + " term.current = true ";//AND "
//              //  + " dueAmount.dueAmount.dueType = :dueType ";
//        LOG.info("Finalquery : {}{}{}", queryString, FROMQuery, WHEREQuery);
//        Query query = getEntityManager().createQuery(queryString+FROMQuery+WHEREQuery, LoansInQueuePojo.class).
//                setParameter("queuePk", queuePk)
//                .setParameter("pendingCCPaymentStatus", CreditCardPaymentStatus.FUTURE_PAYMENT_VERIFIED)
//                //.setParameter("dueType", 99)
//                .setParameter("pendingPtpStatus", PTPStatusCode.PTP_STATUS_PENDING);
//        if(pageNum != null && pageSize != null){
//             query.setFirstResult(pageNum * pageSize);
//             query.setMaxResults(pageSize); 
//        }        
//        return query.getResultList();
//    }
//    public String getLegalQueryForLoan(){
//        return "select new "+LegalDetailsForLoanInQueue.class.getName()+"(legal.litigationDetails.litigationTrialDate as litigationTrialDate,"
//                + " legal.litigationDetails.litigationStipJudgmentDate as litigationStipJudgmentDate,"
//                + " legal.litigationDetails.litigationWritAmount as litigationWritAmount,"
//                + " legal.litigationDetails.litigationExecuteWritDate as litigationExecuteWritDate,"
//                + " courtRef.attorneyName as attorneyName,"
//                + " legal.ccAttorney.ccAttorneyFirm as ccAttorneyFirm,"
//                + " legal.litigationDetails.litigationSheriff,"
//                + " payment.paymentBasicData.paymentEffectiveDate as lastPaymentDate,"
//                + " legal.judgementData.judgementDataTotal as judgementTotal)"
//               
//                + " FROM SvLoan loan"
//                + " JOIN SvLegalData legal"
//                + " LEFT OUTER JOIN legal.svSuperiorCourtReferral courtRef,"
//                + " SvPayment payment"
//               
//                + " WHERE "
//                //+ " loan.pk = :loanPk AND"
//                + " ((payment is null) OR (payment.svLoan = loan AND payment.paymentBasicData.paymentEffectiveDate = (SELECT max(p.paymentBasicData.paymentEffectiveDate) FROM SvPayment p WHERE p.svLoan = loan))) "
//                + " ";
//    }
//    
//    public List<SvLoan> getLoansForQueue(Long queuePk){
//        return getEntityManager().createQuery("SELECT s FROM SvLoan s WHERE s.svLoanCollectionQueue.pk = :queuePk", SvLoan.class).
//                setParameter("queuePk", queuePk).getResultList();
//    }
//    public CustomerAttorney getCustAttorneyPojo(){
//        List<CustomerAttorney> list = getEntityManager().createQuery("select new "+LegalDetailsForLoanInQueue.class.getName()+"(null, null"
//                + ")", CustomerAttorney.class).getResultList();
//        if(list == null || list.isEmpty()){
//            return null;
//        }
//        return list.get(0);
//    }
    public Agent syncAgentWithUser(Agent agent) {
        AmsUser amsUser = accountManagerOWS.getUser(agent.getUserName());
        if (amsUser != null) {
            LOG.info("[AgentRepository] ## User found in AMS");
            agent.setFirstName(amsUser.getFirstName());
            agent.setLastName(amsUser.getLastName());
            if (StringUtils.isNotBlank(amsUser.getPhoneNumber())) {
                agent.setPhoneNumber(Long.parseLong(amsUser.getPhoneNumber().replace("-", "")));
            }
            agent.setPhoneExtension((long) amsUser.getExtension());
            agent.setEmailAddress(amsUser.getEmailAddress());
            agent.setIsActive(amsUser.isActive());
            agent.setLastAccessTime(amsUser.getLastAccessTime());
            agent.setEffectiveCallerId(amsUser.getEffectiveCallerId());
            return agent;
        }
        return null;
    }

//    @SuppressWarnings("unchecked")
//    public List<LoansInQueuePojo> getLoansInQueueUsingNativeQuery(Long queuePk, Integer pageNum, Integer pageSize) {
//        WorkQueue svColQueue = colQRepo.getCollectionsQueue(queuePk);
//        Boolean isSkip = svColQueue.getColQueueData().getPortfolioType() == WorkPortfolioType.COLL_PORTFOLIO_SKIP_TRACE;
//        String query ="";
//        if (isSkip){
//            query = sqlConfigRepo.getQueryBySqlName("GetLoansInSkipQueue");
//        }else{
//            query = sqlConfigRepo.getQueryBySqlName("GetLoansInQueue");
//        }
//        if(StringUtils.isBlank(query)){
//            return null;
//        }
//        if(pageNum != null && pageSize != null){
//            int offset = pageNum*pageSize;
//            String paginationClause = " offset "+offset+" limit "+pageSize;
//            query = query+paginationClause;
//        }
//        Session session = getEntityManager().unwrap(Session.class);
//        LOG.info("getLoansInQueueUsingNativeQuery: {}", query);
//        org.hibernate.Query q = session.createSQLQuery(query);
//        q.setParameter("queuePk", queuePk);
//        q.setResultTransformer(Transformers.aliasToBean(LoansInQueuePojo.class));
//        return q.list();
//    }
}
