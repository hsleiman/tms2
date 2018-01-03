/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vnguyen
 */
public enum Permission {

   
    None(-1, "none", "Permissiion is not required"), //No authentication needed => wide open
    Authenticated(0, "Authenticated", "User must be authenticated"), //Must be authenticated

    Origination_Access_5(5, "Origination - [Access]", "Origination App Access"),
    Servicing_Access_6(6, "Servicing - [Access]", "Servicing App Access"),
    DMS_Access_7(7, "DMS - [Access]", "DMS App Access"),
    Converse_Access_8(8, "Converse - [Access]", "Converse App Access"),
    // AG 11/16/16 Issue #2083 Extended auto-logoff permission
    Extended_Auto_Logoff_20(20, "Extended Auto-Logoff Time", "User has extended time before auto-logoff"),
    //**************************************************************************
    //Role/User/Permssion admin 30-49
    //**************************************************************************
    Roles_View_30(30, "Roles - [View]", "View roles"),
    Permissions_View_31(31, "Permissions - [View]", "View all permissions"),
    RolePermissions_View_32(32, "Role Permissions - [View]", "View role's permissions"),
    RolePermissions_Edit_33(33, "Role Permissions - [Edit]", "Edit role's permissions"),
    RoleUsers_View_34(34, "Role Users - [View]", "View role's users"),
    RoleUsers_Edit_35(35, "Role Users - [Edit]", "Edit role's users"),
    RoleAdminUsers_Edit_36(36, "Role Admin Users - [Edit]", "Edit Admin role's users"),
    User_View_38(38, "User - [View]", "View users"),
    User_Edit_39(39, "User - [Edit]", "Edit user"),
    User_Add_40(40, "User - [Add]", "Add user"),
    UserPassword_Edit_41(41, "User Password - [Edit]", "Update user password"),
    ServicingQueues_Edit_42(42, "Servicing Queue Admin - [Edit]", "Edit servicing queues"),
    UserPhone_Edit_43(43, "User Phone - [Edit]", "Update user phone"),
    TmsAdmin(44, "TMS Admin", "Admin for TMS");
    //Hussien_is_master(44, "Hussien is master", "your not hussien");
    
    private final int id;
    private final String name;
    private final String description;

    private static final Map<Integer, Permission> map = new HashMap<>();

    static {
        for (Permission permission : Permission.values()) {
            map.put(permission.getId(), permission);
        }
    }

    private Permission(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @JsonCreator
    public static Permission fromId(int id) {
        return map.get(id);
    }

    @JsonValue
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /*
     Get the implied permissions for a permission.  
     For example: A user with edit, update, or delete permission should automatically 
     have view perimission
     */
    public static Permission getImpliedPermissions(Permission permission) {
        Permission equivalentPerm = null;
        // AG 1/22/15 - alphabetized so perms are easier to find in the switch statement
        switch (permission) {
            case RolePermissions_Edit_33:
                equivalentPerm = RolePermissions_View_32;
                break;
            case RoleUsers_Edit_35:
                equivalentPerm = RoleUsers_View_34;
                break;

       
            case User_Edit_39:
                equivalentPerm = User_View_38;
                break;
          

        }

        return equivalentPerm;
    }


    
}
