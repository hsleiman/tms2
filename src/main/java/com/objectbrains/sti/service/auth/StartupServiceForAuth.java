package com.objectbrains.sti.service.auth;

import com.objectbrains.ams.iws.*;
import com.objectbrains.scheduler.annotation.RunOnce;
import com.objectbrains.sti.constants.RoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StartupServiceForAuth {

    private static final Logger log = LoggerFactory.getLogger(StartupServiceForAuth.class);
    public final Map<Integer, List<com.objectbrains.sti.constants.Permission>> roleMap = new HashMap<>();
    private final int SYSTEM_ID = 1;
    @Autowired
    private AccountManagerIWS accountManagerIWS;
    @Autowired
    private UserAuth userAuth;

    @PostConstruct
    public void init() {
    }

    @RunOnce(retryOnFailure = true, retryDelayMillis = 30000)
    public void systemStartUp() {
        try {
            createMissingPermission();
            createPermissionRoles();
            linkMissingPermissionsToRole();
            createDefaultUsers();

        } catch (UserNotFoundException | RoleNotFoundException | InvalidFieldException | UserAlreadyExistsException | InvalidPermissionException | InvalidRoleException ex) {
            log.error("Exception {}", ex);
        }

    }

    /**
     * creating evey user in userStrings[] array if not exist
     */
    private void createDefaultUsers() throws UserAlreadyExistsException, InvalidFieldException, RoleNotFoundException, UserNotFoundException {
        //System admin find him and if he exists in am do not nothing otherwise create him with password admin and assign system admin roleType to him.
        //Sam this with username bob. assign settleit agent roleType.
        final String[] userStrings = {"admin", "bob", "farzad", "anand", "alan", "meera", "pavani", "thomas", "tesla"};
        int dummyId = 1;
        for (final String stringUser : userStrings) {
            try {
                //User Exist
                if (accountManagerIWS.getUser(stringUser) == null) {
                    throw new UserNotFoundException("User Not Exist on DB");
                }
            } catch (UserNotFoundException e) {
                //create user
                final int finalDummyId = dummyId++;
                accountManagerIWS.createUser(new CreateUserRequest() {
                    {
                        setUserName(stringUser);
                        setPassword("Password");
                        setPhoneNumber("123123123" + finalDummyId);
                        setDirectRoute(true);
                        setEffectiveCallerId(String.valueOf(finalDummyId));
                        setEmailAddress(stringUser + "@admin.com");
                        setDirectRoute(true);
                        setFirstName(stringUser);
                        setLastName(stringUser);
                        setExtension(finalDummyId + 1201);
                        setExtensionAuthToken(finalDummyId);
                        setFreeswitchIP("127.0.0.1");
                        setFreeswitchPort(finalDummyId + 2000);
                        setTmsIP("127.0.0.1");
                        setTmsPort(finalDummyId + 2200);
                    }
                });

                accountManagerIWS.addRoleToUsers(SYSTEM_ID, RoleType.SYSTEM_ADMIN.getId(), new ArrayList<String>() {
                    {
                        add(stringUser);
                    }
                });
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    private void createMissingPermission() throws InvalidPermissionException {
        List<com.objectbrains.sti.constants.Permission> permissions = userAuth.getAllPermissionsList();
        List<Permission> amsPermissions = accountManagerIWS.getAllPermissions(SYSTEM_ID);
        //for loop find any missing permission in ams and add them.
        for (final com.objectbrains.sti.constants.Permission permission : permissions) {
            if (!isPermissionExistInAmsPermissions(amsPermissions, permission)) {
                accountManagerIWS.createOrUpdatePermission(SYSTEM_ID, new Permission() {
                    {
                        setId(permission.getId());
                        setName(permission.getName());
                        setDescription(permission.getDescription());
                    }
                });
            }
        }
    }

    private boolean isPermissionExistInAmsPermissions(List<Permission> amsPermissions, com.objectbrains.sti.constants.Permission permission) {
        for (Permission p1 : amsPermissions) {
            if (permission.getId() == p1.getId()) {
                return true;
            }
        }
        return false;
    }

    private void createPermissionRoles() throws InvalidRoleException {
        List<RoleType> roleTypes = userAuth.getAllRolesList();
        List<Role> roles = accountManagerIWS.getAllRoles(SYSTEM_ID);

        for (final RoleType roleType : roleTypes) {
            if (!isRoleExist(roles, roleType)) {
                accountManagerIWS.createOrUpdateRole(SYSTEM_ID, new Role() {
                    {
                        setDescription(roleType.getDescription());
                        setName(roleType.getName());
                        setId(roleType.getId());
                    }
                });
            }
        }
    }

    private boolean isRoleExist(List<Role> roles, RoleType roleTypes) {
        for (Role role : roles) {
            if (role.getId() == roleTypes.getId()) {
                return true;
            }
        }
        return false;
    }

    private void linkMissingPermissionsToRole() {

        for (RoleType roleType : RoleType.values()) {
            try {
                List<Permission> permissions = accountManagerIWS.getRolePermissions(SYSTEM_ID, roleType.getId());
                if (permissions == null || permissions.isEmpty()) {
                    throw new RoleNotFoundException("No Permission Found");
                }
            } catch (RoleNotFoundException ex) {
                savePermissionsRole(roleType);
            }
        }

    }


    private void savePermissionsRole(RoleType roleType) {
        List<Integer> permissionIds;
        if (roleType == RoleType.SYSTEM_ADMIN) {
            permissionIds = new ArrayList<Integer>() {
                {
                    for (Object obj : com.objectbrains.sti.constants.Permission.values()) {
                        if (obj instanceof com.objectbrains.sti.constants.Permission) {
                            add(((com.objectbrains.sti.constants.Permission) obj).getId());
                        }
                    }
                }
            };
        } else if (roleType == RoleType.TMS_ADMIN) {
            permissionIds = new ArrayList<>();
        } else {
            permissionIds = new ArrayList<>();
        }

        try {
            accountManagerIWS.setRolePermissions(SYSTEM_ID, RoleType.SYSTEM_ADMIN.getId(), permissionIds);
        } catch (PermissionNotFoundException | RoleNotFoundException e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }
    }

}
