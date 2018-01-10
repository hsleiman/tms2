/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository;

import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.agent.AgentQueue;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.db.entity.agent.Team;
import com.amp.crm.db.repository.utility.SQLConfigRepository;
import com.amp.crm.exception.TooManyObjectFoundException;
import com.amp.crm.ows.AccountManagerOWS;
import com.amp.crm.ows.AmsUser;
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
 * 
 */
@Repository
public class CrmAgentRepository {

    @PersistenceContext
    private EntityManager entityManager;

//    @Autowired
//    private CollectionQueueRepository colQRepo;
    @Autowired
    private AccountManagerOWS accountManagerOWS;

    @Autowired
    private SQLConfigRepository sqlConfigRepo;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CrmAgentRepository.class);

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

    public int getMaxSortNumberInQueue(long queuePk) {
        Query query = getEntityManager().createQuery(
                "SELECT max(s.sortNumberInQueue)FROM SvLoan s WHERE "
                + "s.accountWorkQueue.pk = :queuePk ");
        query.setParameter("queuePk", queuePk);
        return (int) query.getResultList().get(0);

    }

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
