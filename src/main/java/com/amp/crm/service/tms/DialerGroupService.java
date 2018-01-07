/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.tms;

import com.amp.crm.db.entity.agent.Agent;
import com.amp.crm.db.entity.agent.AgentDialerGroup;
import com.amp.crm.db.entity.agent.DialerGroup;
import com.amp.crm.db.entity.base.dialer.DialerQueueGroupAssociation;
import com.amp.crm.db.hibernate.ThreadAttributes;
import com.amp.crm.db.repository.StiAgentRepository;
import com.amp.crm.db.repository.dialer.DialerQueueRepository;
import com.amp.crm.embeddable.AgentDialerGroupInformation;
import com.amp.crm.embeddable.WeightedPriority;
import com.amp.crm.exception.CrmException;
import com.amp.crm.service.dialer.DialerQueueService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author priyankanamburu
 */
@Service
@Transactional
public class DialerGroupService {

    @Autowired
    private StiAgentRepository agentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DialerQueueService dqService;

    @Autowired
    @Lazy
    private DialerQueueRepository dqRepo;

    /**
     *
     * @param dialerGroup
     * @return
     * @throws com.objectbrains.svc.exception.StiException
     */
    public DialerGroup createOrUpdateDialerGroup(DialerGroup dialerGroup) throws CrmException {
        if (dialerGroup == null) {
            throw new CrmException("dialerGroup is null", -1);
        }
        if (dialerGroup.getGroupName() == null || dialerGroup.getGroupName().equalsIgnoreCase("")) {
            throw new CrmException("GroupName cannot be null", -1);
        }
        if (dialerGroup.isIsActive() == null) {
            dialerGroup.setIsActive(Boolean.TRUE);
        }
        dialerGroup.setLastChangedBy("");
        DialerGroup existingGroup = agentRepository.locateByDialerGroupName(dialerGroup.getGroupName());
        if (existingGroup == null) {
            if (dialerGroup.getPk() <= 0) {
                dialerGroup.setComment("DialerGroup created");
                entityManager.persist(dialerGroup);
            } else {
                dialerGroup.setComment("DialerGroup updated");
                entityManager.merge(dialerGroup);
            }
        } else if (existingGroup.getPk() == dialerGroup.getPk()) {
            entityManager.merge(dialerGroup);
        } else {
            throw new CrmException("A DialerGroup " + existingGroup.getPk() + " already exists with groupName " + dialerGroup.getGroupName(), -2);
        }

        return dialerGroup;
    }

    /**
     *
     * @param agentUserName
     * @param weightedPriority
     * @param dialerGroupPk
     * @param isLeader
     * @throws com.objectbrains.svc.exception.StiException
     */
    public void setAgentToDialerGroup(String agentUserName, long dialerGroupPk, WeightedPriority weightedPriority, boolean isLeader, boolean allowAfterHours) throws CrmException {
        if (agentUserName == null || (agentUserName.equals(""))) {
            throw new CrmException("agentUserName is required", -1);
        }
        Agent agent = agentRepository.locateByAgentUserName(agentUserName);
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);
        if (agent == null || dialerGroup == null) {
            throw new CrmException("No Agent and/or dialerGroup found with the given pk/userName", -1);
        }

        if (agent.getAgentDialerGroups() != null && !agent.getAgentDialerGroups().isEmpty()) {
            for (AgentDialerGroup agentDialerGroup : agent.getAgentDialerGroups()) {
                if (agentDialerGroup.getDialerGroup() == dialerGroup) {
                    throw new CrmException("Agent is already assigned to this dialer Group");
                }
            }
        }

        if (!dialerGroup.getSubDialerGroups().isEmpty()) {
            for (DialerGroup svDG : dialerGroup.getSubDialerGroups()) {
                if (svDG.getDialerGroupAgents() != null && !svDG.getDialerGroupAgents().isEmpty()) {
                    for (AgentDialerGroup agentDG : svDG.getDialerGroupAgents()) {
                        if (agentDG.getDialerAgent() == agent) {
                            throw new CrmException("Agent is asigned to one of the subDialerGroups " + svDG + " of the given dialerGroup " + dialerGroup);
                        }
                    }
                }
            }
        }

        AgentDialerGroup agentDG = new AgentDialerGroup();
        agentDG.setDialerAgent(agent);
        agentDG.setDialerGroup(dialerGroup);
        agentDG.setCreatedBy(ThreadAttributes.getString("agent.username"));
        agentDG.setWeightedPriority(weightedPriority);
        agentDG.setLeader(isLeader);
        agentDG.setAllowAfterHours(allowAfterHours);
        entityManager.persist(agentDG);

        agent.getAgentDialerGroups().add(agentDG);
        agent.setAgentDialerGroups(agent.getAgentDialerGroups());
        dialerGroup.getDialerGroupAgents().add(agentDG);
        dialerGroup.setDialerGroupAgents(dialerGroup.getDialerGroupAgents());
        entityManager.merge(agent);
        entityManager.merge(dialerGroup);
    }

    public void addDialerGroupsToDialerGroup(long dialerGroupPk, List<DialerGroup> dialerGroupList) throws CrmException {
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);
        if (dialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + dialerGroupPk + "]", -1);
        }
        if (dialerGroupList == null || dialerGroupList.isEmpty()) {
            throw new CrmException("Given list of dialergroups is null or empty");
        }
        List<DialerGroup> invalidDGList = new ArrayList<>();
        for (DialerGroup svDGroup : dialerGroupList) {
            DialerGroup DG = agentRepository.locateByDialerGroupPk(svDGroup.getPk());
            if (DG != null) {
                //System.out.println("Pk of the given sub group "+svDGroup.getPk());
                svDGroup.getSuperDialerGroups().add(dialerGroup);
                svDGroup.setSuperDialerGroups(svDGroup.getSuperDialerGroups());
                dialerGroup.getSubDialerGroups().add(svDGroup);
                dialerGroup.setSubDialerGroups(dialerGroup.getSubDialerGroups());
            } else {
                invalidDGList.add(svDGroup);
            }

        }
        if (!invalidDGList.isEmpty()) {
            throw new CrmException("The following DialerGroups in the list do not exist: " + invalidDGList + " The rest have been added to the superGroup.");
        }
    }

    /**
     *
     * @param dialerGroupPk
     * @return
     * @throws com.objectbrains.svc.exception.StiException
     */
    public List<Agent> getAllAgentsInDialerGroup(long dialerGroupPk) throws CrmException {
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);
        if (dialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + dialerGroupPk + "]", -1);
        }
        List<Agent> agents = new ArrayList<>();
        if (dialerGroup.getDialerGroupAgents() != null && !dialerGroup.getDialerGroupAgents().isEmpty()) {
            for (AgentDialerGroup agentDG : dialerGroup.getDialerGroupAgents()) {
                agents.add(agentDG.getDialerAgent());
            }
        }
        if (dialerGroup.getSubDialerGroups() != null && !dialerGroup.getSubDialerGroups().isEmpty()) {
            for (DialerGroup svDG : dialerGroup.getSubDialerGroups()) {
                if (svDG.getDialerGroupAgents() != null && !svDG.getDialerGroupAgents().isEmpty()) {
                    for (AgentDialerGroup agentDG : svDG.getDialerGroupAgents()) {
                        agents.add(agentDG.getDialerAgent());
                    }
                }
            }
        }
        return agents;
    }

    /**
     *
     * @param agentUsername
     * @return
     * @throws com.objectbrains.svc.exception.StiException
     */
    public AgentDialerGroupInformation getDialerGroupsForAgent(String agentUsername) throws CrmException {
        if (agentUsername == null || agentUsername.equalsIgnoreCase("")) {
            throw new CrmException("agentUsername is required", -1);
        }
        Agent agent = agentRepository.locateByAgentUserName(agentUsername);
        if (agent == null) {
            throw new CrmException("Cannot locate agent with username [" + agentUsername + "]", -2);
        }
        AgentDialerGroupInformation agentDGPojo = new AgentDialerGroupInformation();
        agentDGPojo.setAgent(agent);

        if (agent.getAgentDialerGroups() != null) {
            List<DialerGroup> dialerGroups = new ArrayList<>();
            for (AgentDialerGroup agentDG : agent.getAgentDialerGroups()) {
                dialerGroups.add(agentDG.getDialerGroup());
                if (agentDG.getDialerGroup() != null && !agentDG.getDialerGroup().getSuperDialerGroups().isEmpty()) {
                    for (DialerGroup svDG : agentDG.getDialerGroup().getSuperDialerGroups()) {
                        dialerGroups.add(svDG);
                    }
                }
            }
            agentDGPojo.setDialerGroups(dialerGroups);
        }
        return agentDGPojo;
    }

    public List<DialerGroup> getAllDialerGroups() {
        return agentRepository.getAllDialerGroups();

    }

    public DialerGroup getDialerGroupByPk(long pk) {
        return agentRepository.locateByDialerGroupPk(pk);

    }

    public void deleteAgentsFromDialerGroup(long dialerGroupPk) throws CrmException {
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);
        if (dialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + dialerGroupPk + "]", -1);
        }
        if (dialerGroup.getDialerGroupAgents() != null) {
            for (AgentDialerGroup agentDG : dialerGroup.getDialerGroupAgents()) {
                //System.out.println("Before removing agent "+agentDG.getDialerAgent()+" from group: "+agentDG.getDialerGroup());
                entityManager.remove(agentDG);
                //System.out.println("After removing agent: "+dialerGroup.getDialerGroupAgents());

            }
        }
        if (!dialerGroup.getSubDialerGroups().isEmpty()) {
            for (DialerGroup svSubDG : dialerGroup.getSubDialerGroups()) {
                svSubDG.getSuperDialerGroups().remove(dialerGroup);
                svSubDG.setSuperDialerGroups(svSubDG.getSuperDialerGroups());
                entityManager.merge(svSubDG);
            }
            dialerGroup.setSubDialerGroups(new HashSet<DialerGroup>());
        }

    }

    public void deleteDialerGroup(long dialerGroupPk) throws CrmException {
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);
        if (dialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + dialerGroupPk + "]", -1);
        }
        if (dialerGroup.getDialerGroupAgents() != null) {
            for (AgentDialerGroup agentDG : dialerGroup.getDialerGroupAgents()) {
                entityManager.remove(agentDG);
            }
        }
        if (!dialerGroup.getSubDialerGroups().isEmpty()) {
            for (DialerGroup svSubDG : dialerGroup.getSubDialerGroups()) {
                svSubDG.getSuperDialerGroups().remove(dialerGroup);
                svSubDG.setSuperDialerGroups(svSubDG.getSuperDialerGroups());
            }
        }
        if (!dialerGroup.getSuperDialerGroups().isEmpty()) {
            for (DialerGroup svSupDG : dialerGroup.getSuperDialerGroups()) {
                if (svSupDG.getSubDialerGroups() != null && !svSupDG.getSubDialerGroups().isEmpty()) {
                    svSupDG.getSubDialerGroups().remove(dialerGroup);
                    svSupDG.setSubDialerGroups(svSupDG.getSubDialerGroups());
                }
            }
        }
        List<DialerQueueGroupAssociation> list = dqRepo.getQueueGroupAssociationByDialerGroup(dialerGroup);
        for (DialerQueueGroupAssociation assoc : list) {
            dqRepo.removeDialerQueueGroupAssociation(assoc);
        }
        entityManager.remove(dialerGroup);
    }

    public void deleteDialerGroupFromGroup(long superDialergroupPk, long subDialerGroupPk) throws CrmException {
        DialerGroup svSuperDialerGroup = agentRepository.locateByDialerGroupPk(superDialergroupPk);
        if (svSuperDialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + superDialergroupPk + "]", -1);
        }
        DialerGroup svSubDialerGroup = agentRepository.locateByDialerGroupPk(subDialerGroupPk);
        if (svSubDialerGroup == null) {
            throw new CrmException("No DialerGroup can be located with the given pk [" + subDialerGroupPk + "]", -1);
        }

        if (svSuperDialerGroup.getSubDialerGroups() == null || svSuperDialerGroup.getSubDialerGroups().isEmpty()) {
            throw new CrmException("The given superDialerGroup does not have any subDialerGroups");
        }

        if (svSuperDialerGroup.getSubDialerGroups().contains(svSubDialerGroup)) {
            svSuperDialerGroup.getSubDialerGroups().remove(svSubDialerGroup);
        }
        if (svSubDialerGroup.getSuperDialerGroups() != null && !svSubDialerGroup.getSuperDialerGroups().isEmpty()) {
            svSubDialerGroup.getSuperDialerGroups().remove(svSuperDialerGroup);
        }

    }

    public void deleteAgentFromAllDialerGroups(String agentUsername) throws CrmException {
        if (agentUsername == null || agentUsername.equalsIgnoreCase("")) {
            throw new CrmException("agentUsername is required", -1);
        }
        Agent agent = agentRepository.locateByAgentUserName(agentUsername);
        if (agent == null) {
            throw new CrmException("Cannot locate agent with username [" + agentUsername + "]", -2);
        }
        for (AgentDialerGroup agentDG : agent.getAgentDialerGroups()) {
            entityManager.remove(agentDG);
        }
    }

    public void deleteAgentFromDialerGroup(String agentUsername, long dialerGroupPk) throws CrmException {
        DialerGroup dialerGroup = agentRepository.locateByDialerGroupPk(dialerGroupPk);

        if (dialerGroup == null) {
            throw new CrmException("Dialer group does not exists", -1);
        }

        if (agentUsername == null || agentUsername.equalsIgnoreCase("")) {
            throw new CrmException("agentUsername is required", -1);
        }
        Agent agent = agentRepository.locateByAgentUserName(agentUsername);

        if (agent == null) {
            throw new CrmException("Cannot locate agent with username [" + agentUsername + "]", -2);
        }

        for (AgentDialerGroup agentDG : agent.getAgentDialerGroups()) {
            if (agentDG.getDialerGroup().getPk() == dialerGroup.getPk()) {
                entityManager.remove(agentDG);
            }
        }
    }

}
