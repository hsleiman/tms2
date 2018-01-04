/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.agent;

import com.objectbrains.ams.iws.AccountManagerIWS;
import com.objectbrains.ams.iws.CreateUserRequest;
import com.objectbrains.ams.iws.FindUsersRequest;
import com.objectbrains.ams.iws.InvalidFieldException;
import com.objectbrains.ams.iws.Permission;
import com.objectbrains.ams.iws.PermissionNotFoundException;
import com.objectbrains.ams.iws.Role;
import com.objectbrains.ams.iws.RoleNotFoundException;
import com.objectbrains.ams.iws.Status;
import com.objectbrains.ams.iws.UpdateUserRequest;
import com.objectbrains.ams.iws.User;
import com.objectbrains.ams.iws.UserAlreadyExistsException;
import com.objectbrains.ams.iws.UserNotFoundException;
import com.objectbrains.sti.constants.SystemId;
import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.db.repository.AgentRepository;
import com.objectbrains.sti.pojo.AgentPojo;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang
 */
@Service
@Transactional
public class AgentService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AgentService.class);

    @Autowired
    private AccountManagerIWS accountManagerIWS;

    @Autowired
    private AgentRepository agentRepo;

    public void syncAllUsersFromAMS() {
        FindUsersRequest fur = new FindUsersRequest();
        //fur.setStatus(Status.ACTIVE);
        List<User> users = accountManagerIWS.findUsers(fur);
        for (User u : users) {
            agentRepo.locateByAgentUserName(u.getUserName());
        }
    }

    public Agent getAgentByPk(long pk) {
        return agentRepo.getAgentByPk(pk);
    }

    public List<Agent> getAllAgents() {
        return agentRepo.getAllAgents();
    }

    public void createAmsUser(CreateUserRequest cur) throws UserAlreadyExistsException, InvalidFieldException {
        accountManagerIWS.createUser(cur);
    }

    public Long updateAmsUser(UpdateUserRequest uur) throws UserNotFoundException, InvalidFieldException {
        Agent agent = agentRepo.locateByAgentUserName(uur.getUserName());
        if (agent == null) {
            throw new UserNotFoundException("Agent does not exist on STI.");
        }
        LOG.info("[AgentService] ## User found in AMS");
        accountManagerIWS.updateUser(uur);
        agent.setFirstName(uur.getFirstName());
        agent.setLastName(uur.getLastName());
        if (StringUtils.isNotBlank(uur.getPhoneNumber())) {
            agent.setPhoneNumber(Long.parseLong(uur.getPhoneNumber().replace("-", "")));
        }
        agent.setPhoneExtension((long) uur.getExtension());
        agent.setEmailAddress(uur.getEmailAddress());
        agent.setEffectiveCallerId(uur.getEffectiveCallerId());
        return agent.getPk();
    }

    public void addPermissionToUser(int systemId, String agentUsername, List<Integer> roleIds, List<Integer> includePermissionIds, List<Integer> excludePermissionIds) throws PermissionNotFoundException, UserNotFoundException, RoleNotFoundException {
        accountManagerIWS.setUserPermissionDetails(systemId, agentUsername, roleIds, includePermissionIds, excludePermissionIds);
    }

    public void addPermissionToRole(int systemId, Integer roleId, List<Integer> permissionIds) throws PermissionNotFoundException, RoleNotFoundException {
        accountManagerIWS.setRolePermissions(systemId, roleId, permissionIds);
    }

    public List<Permission> getAllPermissions(int systemId) {
        return accountManagerIWS.getAllPermissions(systemId);
    }

    public List<Role> getAllRoles(int systemId) {
        return accountManagerIWS.getAllRoles(systemId);
    }

    public List<Role> getUserRoles(int systemId, String username) throws UserNotFoundException {
        return accountManagerIWS.getUserRoles(systemId, username);
    }

    public List<Permission> getUserPermissions(int systemId, String username) throws UserNotFoundException {
        return accountManagerIWS.getUserPermissions(systemId, username);
    }

    public List<Permission> getRolePermissions(int systemId, Integer roleId) throws RoleNotFoundException {
        return accountManagerIWS.getRolePermissions(systemId, roleId);
    }

    //temp fix -- need to talk about method in ams
    public List<Role> getAllRolesWithPermission(int systemId, Integer permissionId) throws RoleNotFoundException {
        List<Role> allRoles = getAllRoles(SystemId.getSystemId());
        List<Role> rolesWithPermission = new ArrayList<>();
        for (Role role : allRoles) {
            List<Permission> permissionsForRole = getRolePermissions(SystemId.getSystemId(), role.getId());
            for (Permission permission : permissionsForRole) {
                if (permission.getId() == permissionId) {
                    rolesWithPermission.add(role);
                }
            }
        }
        return rolesWithPermission;
    }

    public boolean changeAgentPassword(AgentPojo agentPojo) {
        if (agentPojo != null) {
            if (StringUtils.isNotBlank(agentPojo.getAgentUsername()) && StringUtils.isNotBlank(agentPojo.getAgentPassword()) && StringUtils.isNotBlank(agentPojo.getNewAgentPassword())) {
                return accountManagerIWS.changePassword(agentPojo.getAgentUsername(), agentPojo.getAgentPassword(), agentPojo.getNewAgentPassword());
            }
        }
        return false;
    }

    public boolean agentPasswordTest(AgentPojo agentPojo) {
        if (agentPojo != null) {
            if (agentPojo.getAgentPassword() != null && agentPojo.getAgentUsername() != null);
            {
                return accountManagerIWS.isUserPassword(agentPojo.getAgentUsername(), agentPojo.getAgentPassword());
            }
        }
        return false;
    }
}
