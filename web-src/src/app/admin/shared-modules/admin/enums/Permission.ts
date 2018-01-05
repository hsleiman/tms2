export  enum Permission {
    None = -1, //No authentication needed => wide open
    Authenticated = 0, //Must be authenticated

    Origination_Access_5 = 5,
    Servicing_Access_6 = 6,
    DMS_Access_7 = 7,
    Converse_Access_8 = 8,
   
    Extended_Auto_Logoff_20 = 20,
    
    Roles_View_30 = 30,
    Permissions_View_31 = 31,
    RolePermissions_View_32 = 32,
    RolePermissions_Edit_33 = 33,
    RoleUsers_View_34 = 34,
    RoleUsers_Edit_35 = 35,
    RoleAdminUsers_Edit_36 = 36,
    User_View_38 = 38,
    User_Edit_39 = 39,
    User_Add_40 = 40,
    UserPassword_Edit_41 = 41,
    ServicingQueues_Edit_42 = 42,
    UserPhone_Edit_43 = 43,
    TmsAdmin = 44
}