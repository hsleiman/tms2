/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.auditInterceptor;

import com.amp.crm.constants.WorkLogConstants;
import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.superentity.SuperEntityInterface;
import com.amp.crm.db.hibernate.ApplicationContextProvider;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import org.hibernate.type.Type;

/**
 *
 * 
 */
public class ChangedEntity implements Serializable{
    
    private final AuditEntityManager auditEntityManager;
    

    private Object entity;
    private Serializable id;
    private Object[] currentState;
    private Object[] previousState;
    private String[] propertyNames;
    private Type[] types;
    private int changeType;
    
    private Long appPk;
    private Long borrowerPk;
    

    public ChangedEntity(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types, int changeType) {
        this.auditEntityManager = (AuditEntityManager) ApplicationContextProvider.getApplicationContext().getBean(AuditEntityManager.class);
        this.entity = entity;
        this.id = id;
        this.currentState = state;
        this.propertyNames = propertyNames;
        this.types = types;
        this.changeType = changeType;
    }

    public ChangedEntity(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        this.auditEntityManager = (AuditEntityManager) ApplicationContextProvider.getApplicationContext().getBean(AuditEntityManager.class);
        this.entity = entity;
        this.id = id;
        this.currentState = currentState;
        this.previousState = previousState;
        this.propertyNames = propertyNames;
        this.types = types;
        this.changeType = WorkLogConstants.DATA_CHANGE_LOG_UPDATE;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public Object[] getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Object[] currentState) {
        this.currentState = currentState;
    }

    public Object[] getPreviousState() {
        return previousState;
    }

    public void setPreviousState(Object[] previousState) {
        this.previousState = previousState;
    }

    public String[] getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    public Type[] getTypes() {
        return types;
    }

    public void setTypes(Type[] types) {
        this.types = types;
    }

    public int getChangeType() {
        return changeType;
    }

    public void SetChangeType(int changeType) {
        this.changeType = changeType;
    }

    public long getEntityPK() {
        if (entity != null) {
            long pk = ((SuperEntityInterface) entity).getPk();
            return pk;
        }
        return 0;
    }
    
    public String getClassName(){
        if(entity != null) 
            return entity.getClass().getSimpleName();
        return null;
    }

    public long getEntityAssignAppPk() {
        long appPk = 0;
        if (entity != null) {
            for (int i = 0; i < propertyNames.length; i++) {
                Object objectState = currentState[i];
                if(objectState != null){
                    if (objectState instanceof Account) {
                        appPk = ((Account) objectState).getPk();
                    }
                    else if(propertyNames[i].equalsIgnoreCase("accountPk")){
                        appPk = ((long) objectState);
                    }
                }
            }
        }
        return appPk;
    }
    
    public static boolean isGetter(Method method) {
        if (!method.getName().startsWith("get")) {
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        if (void.class.equals(method.getReturnType())) {
            return false;
        }
        return true;
    }

    public Long getAppPk() {
        return appPk;
    }

    public void setAppPk(Long appPk) {
        this.appPk = appPk;
    }

    public Long getBorrowerPk() {
        return borrowerPk;
    }

    public void setBorrowerPk(Long borrowerPk) {
        this.borrowerPk = borrowerPk;
    }

    public HashMap<String,Long> getEntityAppPkAndBorrowerPk() {
        long borrowerPk = 0;
        long appPk = 0;
        if (entity != null) {
            if(entity instanceof Customer){
                borrowerPk = ((Customer)entity).getPk();
            }else if(entity instanceof Account){
                appPk = ((Account) entity).getPk();
            }
            for (int i = 0; i < propertyNames.length; i++) {
                Object objectState = currentState[i]; 
                if(objectState != null){
                    if(borrowerPk == 0){
                        if (objectState instanceof Customer) {                           
                            borrowerPk = ((Customer) objectState).getPk();                           
                        }
                        else if(propertyNames[i].equalsIgnoreCase("clientPk")){                            
                            borrowerPk = ((long) objectState);
                        }
                    }
                    if(appPk == 0){
                        if (objectState instanceof Account) {                           
                            appPk = ((Account) objectState).getPk();
                        }
                        else if(propertyNames[i].equalsIgnoreCase("accountPk")){                            
                            appPk = ((long) objectState);
                        }
                        else{
                            appPk=auditEntityManager.getAccountPkFromClientPk(borrowerPk);
                        }
                    }
                }
            }
            
        }
        HashMap<String,Long> retMap = new HashMap<>();
        setAppPk(appPk);
        setBorrowerPk(borrowerPk);
        retMap.put("accountPk",appPk);       
        retMap.put("clientPk",borrowerPk);
        return retMap;
        
    }

    @Override
    public String toString() {
        return "ChangedObjectGent2{" + "entity=" + entity + ", id=" + id + ", currentState=" + Arrays.toString(currentState) + ", previousState=" + Arrays.toString(previousState) + ", propertyNames=" + Arrays.toString(propertyNames) + ", types=" + Arrays.toString(types) + ", type=" + changeType + '}';
    }
    
    
}
