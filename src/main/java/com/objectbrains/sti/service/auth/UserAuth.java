/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.auth;

import com.hazelcast.core.IMap;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.sti.config.Configs;
import com.objectbrains.sti.constants.Permission;
import com.objectbrains.sti.constants.RoleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hsleiman
 */
@Service
public class UserAuth {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuth.class);
    @Autowired
    private HazelcastService hazelcast;
    private IMap<String, String> userToken;
    private IMap<String, String> tokenUser;
    private IMap<String, List<Integer>> userPermission;

    @PostConstruct
    public void init() {
        userToken = hazelcast.getMap(Configs.USER_TOKEN_KEY_MAP);
        userPermission = hazelcast.getMap(Configs.USER_PERMISSION_KEY_MAP);
        tokenUser = hazelcast.getMap(Configs.TOKEN_USER_KEY_MAP);

        populateRoleMap();
    }

    public boolean userLoggedIn(String username) {
        return userToken.containsKey(username);
    }

    public boolean isValid(String username, String token) {
        LOG.debug("Check user is valid {} for token {}", username, token);
        if (token == null || username == null) {
            return false;
        }

        if (userToken.containsKey(username)) {
            LOG.debug("Found user is valid {} for token {}", username, token);
            String tokenAuth = userToken.get(username);
            if (tokenAuth.equals(token)) {
                LOG.debug("Valid user {} for token {}", username, token);
                return true;
            }
        }
        LOG.debug("Invalid user {} for token {}", username, token);
        return false;
    }

    public void addTokenForUser(String username, String token, List<com.objectbrains.ams.iws.Permission> permissions) {
        List<Integer> per = new ArrayList<>();
        for (int i = 0; i < permissions.size(); i++) {
            com.objectbrains.ams.iws.Permission get = permissions.get(i);
            per.add(get.getId());
        }
        userToken.put(username, token);
        tokenUser.put(token, username);
        userPermission.put(username, per);
    }

    public void clearTokenForUser(String username) {
        String token = userToken.get(username);
        userToken.remove(username);
        tokenUser.remove(token);
        userPermission.remove(username);

    }

    public boolean hasPermission(String username, Integer permission) throws Exception {
        List<Integer> per = userPermission.get(username);
        if (per != null) {
            return per.contains(permission);
        }
        return false;
    }

    public String getTokenForUsername(String username) {
        return userToken.get(username);
    }

    public String getUsernameForToken(String token) {
        String username = tokenUser.get(token);
        if (username == null) {
            for (Map.Entry<String, String> entry : userToken.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value.equals(token)) {
                    tokenUser.put(token, username);
                    username = key;
                }
            }
        }
        return username;
    }

    public List<Permission> getAllPermissionsList() {
        return new ArrayList<Permission>() {
            {
                for (Object obj : Permission.values()) {
                    if (obj instanceof Permission) {
                        add((Permission) obj);
                    }
                }
            }
        };
    }

    public List<RoleType> getAllRolesList() {
        return new ArrayList<RoleType>() {
            {
                for (Object obj : RoleType.values()) {
                    if (obj instanceof RoleType) {
                        add((RoleType) obj);
                    }
                }
            }
        };
    }

    public List<Permission> getPermissionByRoleType(final RoleType roleType) throws Exception {

        switch (roleType) {
            case SYSTEM_ADMIN:
                return getAllPermissionsList();
            case SETTLEIT_AGENT:
                return new ArrayList<Permission>() {
                    {
                        add(Permission.Authenticated);
                    }
                };
            default:
                throw new Exception("Invalid Roll as:" + roleType);
        }

    }


    /**
     * <B>Hard coded</B> init of RoleMap, assigned permissions for each role
     */
    private void populateRoleMap() {
        
        Map<Integer, List<Integer>> roleMap = new ConcurrentHashMap<>();

        roleMap.put(RoleType.SYSTEM_ADMIN.getId(), new ArrayList<Integer>() {{
            //system admin has all permission ids (Super User)
            addAll(new ArrayList<Integer>(){{
                for (com.objectbrains.sti.constants.Permission permission : getAllPermissionsList())
                    add(permission.getId());
            }});
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.SETTLEIT_AGENT.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.SETTLEIT_AGENT_SUPERVISOR.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.SETTLEIT_AGENT_MANAGER.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.SETTLEIT_AGENT_BACKEND.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.SETTLEIT_SERVICE.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.PAYMENT_PROCESSOR.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.PAYMENT_PROCESSOR_SUPERVISOR.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.AUDITOR.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.TMS_ADMIN.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});

        //// TODO: 12/10/17 permission should be modified
        roleMap.put(RoleType.TMS_MANAGER.getId(), new ArrayList<Integer>() {{
            add(com.objectbrains.sti.constants.Permission.Origination_Access_5.getId());
            add(com.objectbrains.sti.constants.Permission.Servicing_Access_6.getId());
            add(com.objectbrains.sti.constants.Permission.DMS_Access_7.getId());
        }});
    }

}
