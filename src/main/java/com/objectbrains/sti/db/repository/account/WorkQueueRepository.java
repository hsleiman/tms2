/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.db.repository.account;

import com.objectbrains.sti.constants.WorkQueueType;
import com.objectbrains.sti.db.entity.base.account.Account;
import com.objectbrains.sti.db.entity.base.AccountQueueMovement;
import com.objectbrains.sti.db.entity.base.WorkQueue;
import com.objectbrains.sti.embeddable.WorkQueueData;
import com.objectbrains.sti.embeddable.LastAssignmentData;
import com.objectbrains.sti.exception.WorkQueueException;
import com.objectbrains.sti.exception.ObjectNotFoundException;
import com.objectbrains.sti.exception.TooManyObjectFoundException;
import com.objectbrains.sti.service.core.AccountService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author David
 */
@Repository
public class WorkQueueRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private AccountRepository accountRepo;
    
    private static Logger LOG = LoggerFactory.getLogger(WorkQueueRepository.class);

    EntityManager getEntityManager() {
        return entityManager;
    }

    public WorkQueue getWorkQueue(long queuePk) {
        List<WorkQueue> res = getEntityManager().createNamedQuery("WorkQueue.LocateByPk", WorkQueue.class).
                setParameter("pk", queuePk).getResultList();
        if ((res == null) || (res.isEmpty())) {
            throw new ObjectNotFoundException("Work Queue Not found with given Pk : " + queuePk);
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many Queues with given Id");
        }
        return res.get(0);
    }
    
    public WorkQueue getWorkQueueByQueueName(String queueName) {
        List<WorkQueue> res = getEntityManager().createNamedQuery("WorkQueue.LocateByQueueName", WorkQueue.class).
                setParameter("queueName", queueName).getResultList();
        if ((res == null) || (res.isEmpty())) {
            throw new ObjectNotFoundException("Work Queue Not found with given queue name  : " + queueName);
        }
        if (res.size() > 1) {
            throw new TooManyObjectFoundException("Found too many Queues with given queue name "+queueName);
        }
        return res.get(0);
    }

    public List<WorkQueue> getAllQueues() {
        List<WorkQueue> res = getEntityManager().createNamedQuery("WorkQueue.LocateAll", WorkQueue.class).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }

        return res;
    }

    public List<WorkQueue> getAllActiveQueues() {
        List<WorkQueue> res = getEntityManager().createNamedQuery("WorkQueue.LocateAllActiveQueues", WorkQueue.class).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }

        return res;
    }
    
    public List<Account> getAccounts(Long queuePk){
        List<Account> res = getEntityManager().createNamedQuery("Account.LocateByQueuePk", Account.class).setParameter("pk", queuePk).getResultList();
        if ((res == null) || (res.isEmpty())) {
            return new ArrayList<>();
        }

        return res;
    }
    public List<LastAssignmentData> getAssignmentDataForDefaultPortfolioQueues(long portfolioId) {
        Query query = getEntityManager().
                createNativeQuery( 
                    "SELECT q.pk, MAX(s.create_timestamp) " +
                    "FROM sti.work_queue q " +
                    "LEFT OUTER JOIN sti.account_queue_movement s ON s.new_queue_pk = q.pk " +
                    "WHERE  q.portfolio_type = :portfolio " +
                        "AND COALESCE(q.default_portfolio_queue,false) = true " +
                        "AND COALESCE(q.active,false) = true " + 
                    "GROUP BY q.pk " ).
                setParameter("portfolio", new Long(portfolioId));
                
        @SuppressWarnings("unchecked")
        List<Object[]> res = query.getResultList();
        
        List<LastAssignmentData> ladList = new ArrayList<>();
        for (Object[] o : res) {
            long pk = 0;
            if (o[0] instanceof Number) pk = ((Number) o[0]).longValue();
            else throw new WorkQueueException("Expected Number for PK, got a(n) " + o[0].getClass().getName());
            
            LocalDateTime dt = null;
            if (o[1] != null) {
                if (o[1] instanceof Timestamp) dt = new LocalDateTime(((Timestamp) o[1]).getTime());
                else throw new WorkQueueException("Expected Timestamp for date, got a(n) " + o[1].getClass().getName());
            }
            
            ladList.add( new LastAssignmentData( pk, dt ) );
        }
        return ladList;
    }
    
    /**
     * Find the next portfolio default queue to assign to
     * 
     * @param portfolioId
     * @return portfolio that hasn't been assigned in the longest time.
     * @throws ObjectNotFoundException
     * @throws TooManyObjectFoundException
     */
    public WorkQueue getDefaultPortfolioQueueRoundRobin(long portfolioId) {
        
        LocalDateTime earliestTime = null;
        long earliestPk = 0;
        
        List<LastAssignmentData> ladList = getAssignmentDataForDefaultPortfolioQueues(portfolioId);
        for (LastAssignmentData lad : ladList) {
            if (lad.getLastAssignmentTime() == null) return getWorkQueue(lad.getQueuePk());
            if ((earliestTime == null) || (earliestTime.isAfter(lad.getLastAssignmentTime()))) {
                earliestPk = lad.getQueuePk();
                earliestTime = lad.getLastAssignmentTime();
            }
        }
        
        if (earliestTime == null) {
           throw new WorkQueueException("There is no usable default Portfolio queue on portfolio " + portfolioId);
        } else {
            return getWorkQueue(earliestPk);
        }
    }
    
    public WorkQueue getPortfolioQueue(long portfolioType, String queueName) {
        Query query = getEntityManager().createQuery("SELECT s FROM WorkQueue s WHERE UPPER(REGEXP_REPLACE(s.workQueueData.queueName,'[\\s]+',''))  = UPPER(REGEXP_REPLACE(:queueName,'[\\s]+','')) AND s.workQueueData.portfolioType = :portfolioType", WorkQueue.class);
        query.setParameter("queueName", queueName);
        query.setParameter("portfolioType", portfolioType);
        @SuppressWarnings("unchecked")
        ArrayList<WorkQueue> res = (ArrayList<WorkQueue>) query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }

        return res.get(0);
    }

    public List<WorkQueue> getAllQueuesByPortfolioType(long portfolioType) {
        Query query = getEntityManager().createQuery("SELECT s FROM WorkQueue s WHERE s.workQueueData.portfolioType = :portfolioType", WorkQueue.class);
        query.setParameter("portfolioType", portfolioType);
        @SuppressWarnings("unchecked")
        ArrayList<WorkQueue> res = (ArrayList<WorkQueue>) query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        return res;
    }
    
    public List<WorkQueue> getActiveQueuesByPortfolioType(Long portfolioType) {
        Query query = getEntityManager().createQuery("SELECT c FROM WorkQueue c WHERE c.workQueueData.portfolioType = :portfolioType AND c.workQueueData.active = TRUE", WorkQueue.class);
        query.setParameter("portfolioType", portfolioType);
        @SuppressWarnings("unchecked")
        ArrayList<WorkQueue> res = (ArrayList<WorkQueue>) query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        return res;
    }
    
    public List<Account> getQueueAccountsOrderBy(Long queuePk, String orderByField) {
        String q = "SELECT s FROM Account s, WorkQueue c WHERE c.pk=:pk AND s.accountWorkQueue.pk=c.pk ORDER BY s."+orderByField+" ASC";
        Query query = getEntityManager().createQuery(q, Account.class);
        query.setParameter("pk", queuePk);
        @SuppressWarnings("unchecked")
        ArrayList<Account> res = (ArrayList<Account>) query.getResultList();
        if ((res == null) || (res.isEmpty())) {
            return null;
        }
        return res;
    }
    
    public WorkQueue locateByWorkQueuePk(long queuePk) {
        WorkQueue queue = entityManager.find(WorkQueue.class, queuePk);
        if (queue == null) {
            throw new ObjectNotFoundException(queuePk, WorkQueue.class);
        }
        return queue;
    }

    public List<Account> locateAccountListByqueuePkAndSortNumber(long queuePk, int sortNumber) {
        Query query = getEntityManager().createQuery(
                "SELECT s FROM Account s WHERE "
                        + "s.AccountWorkQueue.pk = :queuePk AND "
                        + "s.sortNumberInQueue > :sortNumber"
                        + " ORDER BY s.sortNumberInQueue ASC");
        query.setParameter("queuePk", queuePk);
        query.setParameter("sortNumber", sortNumber);
        @SuppressWarnings("unchecked")
        List<Account> res = query.getResultList();
        return res;
    }
    
    public Account getNextAccountInQueue(WorkQueue workQueue){
        TypedQuery<Account> query = getEntityManager().createQuery(
                "SELECT s FROM Account s WHERE "
                        + "s.accountWorkQueue.pk = :queuePk AND "
                        + "s.sortNumberInQueue > :sortNumber"
                        + " ORDER BY s.sortNumberInQueue ASC",
                Account.class);
        query.setParameter("queuePk", workQueue.getPk());
        query.setParameter("sortNumber", workQueue.getWorkQueueData().getLastReturnedAccountSortNumber());
        @SuppressWarnings("unchecked")
        List<Account> res = query.getResultList();
        if(res == null || res.isEmpty()){
            query.setParameter("queuePk", workQueue.getPk());
            query.setParameter("sortNumber", 0);
            res = query.getResultList();
        }
        if(res != null && !res.isEmpty()){
            return res.get(0);
        }
        return null;
    }
    

    
    public Account locateAccountByqueuePkAndSortNumber(long queuePk, int sortNumber) {
        Query query = getEntityManager().createQuery(
                "SELECT s FROM Account s WHERE "
                        + "s.accountWorkQueue.pk = :queuePk AND "
                        + "s.sortNumberInQueue = :sortNumber "
                        );
        query.setParameter("queuePk", queuePk);
        query.setParameter("sortNumber", sortNumber);
        @SuppressWarnings("unchecked")
        List<Account> res = query.getResultList();
        if(res == null || res.isEmpty()){
            return null;
        }
        if(res.size() > 1){
            throw new TooManyObjectFoundException("Too Many Accounts found with the same queuePk "+queuePk+" and sortNumber : "+sortNumber);
        }
        return res.get(0);
    }
    
    public int getMaxSortNumberInQueue(long queuePk){
        Query query = getEntityManager().createQuery(
                "SELECT max(s.sortNumberInQueue)FROM Account s WHERE "
                        + "s.accountWorkQueue.pk = :queuePk ");
        query.setParameter("queuePk",queuePk);
        if(query.getResultList() == null || query.getResultList().isEmpty() || query.getResultList().get(0)==null){
            return 0;
        }
        return (int) query.getResultList().get(0);
        
    }
    
    public List<WorkQueue> getActiveAutomaticWorkQueuesByPortfioAndQueueType(long portfolioType, Boolean isActive){
        Query query = getEntityManager().createQuery(
                "SELECT s FROM WorkQueue s WHERE "
                        + " s.workcolQueueData.portfolioType = :portfolioType AND "
                        + " s.workQueueData.active = :isActive AND "
                        + " (s.workQueueData.manual = :isManual OR s.workQueueData.manual = NULL)");
        query.setParameter("portfolioType", portfolioType);
        //query.setParameter("queueType", queueType);
        query.setParameter("isActive", isActive);
        query.setParameter("isManual", Boolean.FALSE);
        @SuppressWarnings("unchecked")
        List<WorkQueue> res = query.getResultList();
        return res;
    }
    
    public WorkQueue createWorkQueue(WorkQueueData workQueueData) {
        WorkQueue workQueue = new WorkQueue();
        workQueue.setWorkQueueData(workQueueData);
        getEntityManager().persist(workQueue);
        return workQueue;
    }
    
    @SuppressWarnings("unchecked")
    public List<Long> getUnprocessedQueueSortAccounts(){
        List<Long> list = getEntityManager().createQuery("SELECT s.accountPk FROM AccountChangeLog WHERE s.status = 0 GROUP BY s.accountPk").getResultList();
        return list;
    }
    
    public List<AccountChangeLog> getUnprocessedQueueSortLogs(){
        List<AccountChangeLog> list = getEntityManager().createNamedQuery("AccountChangeLog.locateAllUnprocessedLogs", AccountChangeLog.class).getResultList();
        return list;
    }
    
    public List<GetNextAccountCriteria> getSortCriteriaForAccount(long accountPk){
        List<GetNextAccountCriteria> list = getEntityManager().createNamedQuery("GetNextAccountCriteria.locateByAccountPk", GetNextAccountCriteria.class)
                .setParameter("accountPk", accountPk).getResultList();
        return list;
    }

    public List<GetNextAccountCriteria> getSortCriteriaForQueue(long queuePk){
        List<GetNextAccountCriteria> list = getEntityManager().createQuery("SELECT s FROM GetNextAccountCriteria s WHERE s.queuePk = :queuePk", GetNextAccountCriteria.class)
                .setParameter("queuePk", queuePk).getResultList();
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<AccountQueueMovement> getSortedQueueMovesInPortfolio(long accountPk, long portfolioId) {
        Account account = accountService.getAccountByPk(accountPk);
        String sqlQuery = "SELECT s FROM AccountQueueMovement s WHERE s.account = :account AND s.queueMovementPojo.newPortfolio = :portfolioId";
        sqlQuery = sqlQuery.concat(" ORDER BY s.queueMovementPojo.createTimestamp DESC");
        TypedQuery<AccountQueueMovement> query = entityManager.createQuery(sqlQuery, AccountQueueMovement.class);
        query.setParameter("account", account);
        query.setParameter("portfolioId", portfolioId);
        return query.getResultList();
    }
    
    public List<WorkQueue> getAllDefaultWorkQueues(){
        return getEntityManager().createQuery("SELECT s FROM WorkQueue s WHERE s.queuetype = :queueType AND s.queueName like \'Default%\'", WorkQueue.class).
                setParameter("queueType", WorkQueueType.WORK_DEFAULT_PORTFOLIO_QUEUE).getResultList();
    }
    
    public AccountQueueMovement getLatestQueueMovementForAccount(long accountPk, long newPortfolioId){
        AccountQueueMovement res;
        try{
            TypedQuery<AccountQueueMovement> query = entityManager.createNamedQuery("AccountQueueMovement.LatestQueueMovementForAccount", AccountQueueMovement.class);
            query.setParameter("accountPk", accountPk);
            query.setParameter("newPortfolio", newPortfolioId);
            query.setMaxResults(1);
            res = query.getSingleResult();
        }catch(NoResultException ex){
            res=null;
        }
        return res;
    }


    
    public void updateQueueCount(){
        getEntityManager().createNativeQuery("update " +
            "sti.work_queue q " +
            "set queue_count = (select count(*) from sti.account account where  account.work_queue_pk = q.pk) " +
            "where queue_name not like '%Skip%'").executeUpdate();
    }
}

