/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import com.objectbrains.ams.iws.User;
import com.amp.tms.db.entity.AgentRecord;
import com.amp.tms.service.AmsService;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hsleiman
 */
@Repository
@Transactional
public class AgentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AgentRepository.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AgentQueueAssociationRepository agentQueueAssocRepo;

    @Autowired
    private AgentStatsRepository statsRepository;

    @Autowired
    @Lazy
    private AgentCallRepository callRepository;

    @Autowired
    @Lazy
    private AmsService amsService;

    public AgentRecord createFromUser(User user) {
        AgentRecord newAgent;
        AgentRecord oldAgent = findAgent(user.getUserName());
        if (oldAgent != null) {
            if (oldAgent.getExtension() == user.getExtension()) {
                return oldAgent;
            }
            oldAgent.setUserName(user.getUserName() + ".deleting");
            entityManager.flush();
        }
        try {
            InetAddress tmsAddress = InetAddress.getByName(user.getTmsIP());
            if (NetworkInterface.getByInetAddress(tmsAddress) == null) {
                //This user does not belong to this TMS
                //TODO throw error or something
            }
        } catch (UnknownHostException | SocketException ex) {
            LOG.error(ex.getMessage(), ex);
            return null;
        }
        newAgent = new AgentRecord(user);
        entityManager.persist(newAgent);

        if (oldAgent != null) {
            agentQueueAssocRepo.reassignAssociations(oldAgent, newAgent);
            statsRepository.reassignAssociations(oldAgent, newAgent);
            callRepository.reassignAssociations(oldAgent, newAgent);
            entityManager.flush();
            entityManager.remove(oldAgent);
        }

        return newAgent;
    }
    
    public void changeUserName(int ext, String newUserName){
        AgentRecord agent = getAgent(ext);
        agent.setUserName(newUserName);
    }

//    private AgentRecord getAgent(String userName) {
//        AgentRecord agent = findAgent(userName);
//        if (agent != null) {
//            return agent;
//        }
//        User user = amsService.getUser(userName);
//        if (user != null) {
//            return createFromUser(user);
//        }
//        return null;
//    }
    private AgentRecord findAgent(String userName) {
        return (AgentRecord) entityManager.unwrap(Session.class)
                .bySimpleNaturalId(AgentRecord.class)
                //                .with(LockOptions.UPGRADE)
                .load(userName.toLowerCase());
//        try {
//            
//            
//            return entityManager.createQuery("select agent from Agent agent where agent.userName = :userName", AgentRecord.class)
//                    .setParameter("userName", userName.toLowerCase())
//                    .getSingleResult();
//        } catch (NoResultException ex) {
//            return null;
//        }
    }

    private AgentRecord createRecord(int agentExt) {
        LOG.info("agent {} not found, attempting to load from ams", agentExt);
        User user = amsService.getUser(agentExt);
        LOG.info("loaded agent {} from ams", agentExt);
        if (user != null) {
            return createFromUser(user);
        }
        return null;
    }

    public AgentRecord getAgent(Integer agentExt) {
        AgentRecord agent = entityManager.find(AgentRecord.class, agentExt);
        if (agent != null) {
            return agent;
        }
        return createRecord(agentExt);
    }

    public List<AgentRecord> getAgents(Collection<Integer> keys) {
        List<AgentRecord> records = new ArrayList<>(entityManager.createQuery(
                "select agent from Agent agent where agent.extension in (:extensions)", AgentRecord.class)
                .setParameter("extensions", keys)
                .getResultList());
        Set<Integer> extentionsNotFound = new HashSet<>(keys);
        for (AgentRecord record : records) {
            extentionsNotFound.remove(record.getExtension());
        }
        for (Integer agentExt : extentionsNotFound) {
            AgentRecord record = createRecord(agentExt);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

}
