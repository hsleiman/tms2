/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.rest;

import com.objectbrains.ams.iws.CreateUserRequest;
import com.objectbrains.ams.iws.InvalidFieldException;
import com.objectbrains.ams.iws.PermissionNotFoundException;
import com.objectbrains.ams.iws.Role;
import com.objectbrains.ams.iws.RoleNotFoundException;
import com.objectbrains.ams.iws.UpdateUserRequest;
import com.objectbrains.ams.iws.UserAlreadyExistsException;
import com.objectbrains.ams.iws.UserNotFoundException;
import com.objectbrains.sti.aop.Authorization;
import com.objectbrains.sti.constants.Permission;
import com.objectbrains.sti.constants.SystemId;
import com.objectbrains.sti.db.entity.agent.Agent;
import com.objectbrains.sti.pojo.AgentPojo;
import com.objectbrains.sti.pojo.RolePermission;
import com.objectbrains.sti.pojo.UserPermission;
import com.objectbrains.sti.service.agent.StiAgentService;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author David
 */
@RestController()
@RequestMapping(value = "/amsRestController", produces = MediaType.APPLICATION_JSON_VALUE)
public class AmsRestController {

    @Autowired
    private StiAgentService agentService;
    
    @RequestMapping(value = "/syncAgents", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void syncAgents() {
        agentService.syncAllUsersFromAMS();
    }

    @RequestMapping(value = "/getAllAgents", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<Agent> getAllAgents() throws IOException {
        return agentService.getAllAgents();
    }
    
    @RequestMapping(value = "/getAgentByPk/{agentPk}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public Agent getAgentByPk(@PathVariable long agentPk) throws IOException {
        return agentService.getAgentByPk(agentPk);
    }


    @RequestMapping(value = "/createAmsUser", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean createAmsUser(@RequestBody CreateUserRequest createUserRequest) throws IOException, UserAlreadyExistsException, InvalidFieldException {
        agentService.createAmsUser(createUserRequest);
        agentService.syncAllUsersFromAMS();
        return true;
    }
    
    @RequestMapping(value = "/updateAmsUser", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public void updateAmsUser(@RequestBody UpdateUserRequest updateUserRequest) throws IOException, UserAlreadyExistsException, InvalidFieldException, UserNotFoundException {
        agentService.updateAmsUser(updateUserRequest);
        agentService.syncAllUsersFromAMS();
    }
    
    @RequestMapping(value = "/addPermissionToUser", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean addPermissionToUser(@RequestBody UserPermission userPermission) throws PermissionNotFoundException, UserNotFoundException, RoleNotFoundException {
        agentService.addPermissionToUser(SystemId.getSystemId(), userPermission.getAgentUsername(), userPermission.getRoleIds(), userPermission.getIncludePermissionIds(), userPermission.getExcludePermissionIds());
        return true;
    }

    @RequestMapping(value = "/addPermissionToRole", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean addPermissionToRole(@RequestBody RolePermission rolePermission) throws PermissionNotFoundException, UserNotFoundException, RoleNotFoundException {
        agentService.addPermissionToRole(SystemId.getSystemId(), rolePermission.getRoleId(), rolePermission.getPermissionIds());
        return true;
    }

    @RequestMapping(value = "/getAllPermissions", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<com.objectbrains.ams.iws.Permission> getAllPermissions() {
        return agentService.getAllPermissions(SystemId.getSystemId());
    }

    @RequestMapping(value = "/getAllRoles", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<Role> getAllRoles() {
        return agentService.getAllRoles(SystemId.getSystemId());
    }

    @RequestMapping(value = "/getUserRoles/{username}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<Role> getUserRoles(@PathVariable String username) throws UserNotFoundException {
        return agentService.getUserRoles(SystemId.getSystemId(), username);
    }

    @RequestMapping(value = "/getUserPermissions/{username}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<com.objectbrains.ams.iws.Permission> getUserPermissions(@PathVariable String username) throws UserNotFoundException {
        return agentService.getUserPermissions(SystemId.getSystemId(), username);
    }

    @RequestMapping(value = "/getRolePermissions/{roleId}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<com.objectbrains.ams.iws.Permission> getRolePermissions(@PathVariable Integer roleId) throws RoleNotFoundException {
        return agentService.getRolePermissions(SystemId.getSystemId(), roleId);
    }

    @RequestMapping(value = "/getAllRolesWithPermission/{permissionId}", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public List<Role> getAllRolesWithPermission(@PathVariable Integer permissionId) throws RoleNotFoundException {
        return agentService.getAllRolesWithPermission(SystemId.getSystemId(), permissionId);
    }

    @RequestMapping(value = "/changeAgentPassword", method = POST)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Not Authenticated.")
    public boolean changeAgentPassword(@RequestBody AgentPojo agentPojo){
        return agentService.changeAgentPassword(agentPojo);
    }
    
    @RequestMapping(value = "/agentPasswordTest", method = POST)
    @Authorization(permission = Permission.None, noPermissionTo = "Not Authenticated.")
    public boolean agentPasswordTest(@RequestBody AgentPojo agentPojo){
        return agentService.agentPasswordTest(agentPojo);
    }
    
    //TEST
    @RequestMapping(value = "/testall", method = GET)
    @Authorization(permission = Permission.Authenticated, noPermissionTo = "Somewhat working alls.")
    public ResponseEntity testall() {
        return new ResponseEntity("Test", HttpStatus.OK);
    }

    @RequestMapping(value = "/testnone", method = GET)
    @Authorization(permission = Permission.None, noPermissionTo = "Somewhat working none.")
    public ResponseEntity testnone() {
        return new ResponseEntity("Test", HttpStatus.OK);
    }

    @RequestMapping(value = "/testnoperm", method = GET)
    public ResponseEntity testnoperm() {
        return new ResponseEntity("Test", HttpStatus.OK);
    }

}
