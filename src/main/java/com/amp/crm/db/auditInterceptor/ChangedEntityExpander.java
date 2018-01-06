/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.auditInterceptor;


import com.amp.crm.db.entity.base.account.Account;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.superentity.SuperEntityInterface;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hsleiman
 */
public class ChangedEntityExpander implements Serializable {

    private Object entity;
    private Serializable id;
    private Object[] currentState;
    private Object[] previousState;
    private String[] propertyNames;
    private String[] types;
    private int changeType;

    private Long accountPk;
    private Long clientPk;

    private ArrayList<Object> currentStateList;
    private ArrayList<Object> previousStateList;
    private ArrayList<String> propertyNamesList;
    private ArrayList<String> typesList;
    
    @Autowired
    private AuditEntityManager auditEntityManager;

    private static final Logger LOG = LoggerFactory.getLogger(ChangedEntityExpander.class);
    
    public ChangedEntityExpander(ChangedEntity ce) {
        this.entity = ce.getEntity();
        this.changeType = ce.getChangeType();
        this.accountPk = ce.getAppPk();
        this.clientPk = ce.getBorrowerPk();
        currentStateList = new ArrayList<>();
        previousStateList = new ArrayList<>();
        propertyNamesList = new ArrayList<>();
        typesList = new ArrayList<>();

        expand(ce.getCurrentState(), ce.getPreviousState(), ce.getPropertyNames(), ce.getTypes());
        currentState = currentStateList.toArray();
        previousState = previousStateList.toArray();
        propertyNames = propertyNamesList.toArray(new String[propertyNamesList.size()]);
        types = typesList.toArray(new String[typesList.size()]);

    }

    private void expand(final Object[] currentStateCE, final Object[] previousStateCE, String[] propertyNamesCE, Type[] typesCE) {
        for (int i = 0; i < propertyNamesCE.length; i++) {
            LOG.info("Type : {} is Componenttype? {}", typesCE[i], typesCE[i] instanceof ComponentType);
            if ((typesCE[i] instanceof ComponentType) && ((ComponentType) typesCE[i]).isEmbedded()) {
                LOG.info("ComponenType {} is embedded.", (ComponentType) typesCE[i]);
                DiffNode diff = ObjectDifferBuilder.buildDefault().compare(currentStateCE[i], previousStateCE[i]);
                if (diff.hasChanges()) {
                    final Object previousStateFinal = previousStateCE[i];
                    final Object currentStateFinal = currentStateCE[i];
                    final Type typeFinal = typesCE[i];
                    
                    diff.visit(new DiffNode.Visitor() {
                        @Override
                        public void node(DiffNode node, Visit visit) {
                            String embPropertyName = node.getPath().toString();
                            final Object previousValue = node.canonicalGet(previousStateFinal);
                            final Object currentValue = node.canonicalGet(currentStateFinal);
                            if (previousValue != null) {    
                                if (previousValue.equals(currentValue) == false) {
                                
                                    addItem(currentValue, previousValue, embPropertyName, currentValue.getClass().getSimpleName());
                                }
                            }
                            else if(currentValue != null){
                                addItem(currentValue, previousValue, embPropertyName,  currentValue.getClass().getSimpleName());
                            }
                        }
                    });
                }

            } else {
                addItem(currentStateCE !=null ? currentStateCE[i] : null, previousStateCE != null ? previousStateCE[i] : null, propertyNamesCE[i], typesCE != null ? typesCE[i].getName() : null);
            }
        }
    }

    private void addItem(Object currentStateCE, Object previousStateCE, String propertyNamesCE, String typesCE) {
        currentStateList.add(currentStateCE);
        previousStateList.add(previousStateCE);
        propertyNamesList.add(propertyNamesCE);
        typesList.add(typesCE);
        if(accountPk == null){
            if (currentStateCE != null) {
                if (currentStateCE instanceof Account) {
                    accountPk = ((Account) currentStateCE).getPk();
                } else if (propertyNamesCE.equalsIgnoreCase("accountPk")) {
                    accountPk = ((long) currentStateCE);
                }else if (propertyNamesCE.equalsIgnoreCase("clientPk")) {
                    accountPk = auditEntityManager.getAccountPkFromClientPk(((long) currentStateCE));
                }else if (currentStateCE instanceof Customer) {
                    Customer client = (Customer) currentStateCE;
//                    accountPk = client.getAccount.getPk();//TODO 
                }
            }
        }
        LOG.info("addItem currentStateCE {} previousStateCE {} propertyNamesCE {} typesCE {} appPk: {}", currentStateCE, previousStateCE, propertyNamesCE, typesCE, accountPk);
        
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

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
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

    public String getClassName() {
        if (entity != null) {
            return entity.getClass().getSimpleName();
        }
        return null;
    }

    public long getEntityAssignAppPk() {
        long appPk = 0;
        if (entity != null) {
            for (int i = 0; i < propertyNames.length; i++) {
                Object objectState = currentState[i];
                if (objectState != null) {
                    if (objectState instanceof Account) {
                        appPk = ((Account) objectState).getPk();
                    } else if (propertyNames[i].equalsIgnoreCase("accountPk")) {
                        appPk = ((long) objectState);
                    }
                }
            }
        }
        return appPk;
    }

    private boolean isGetter(Method method) {
        if (!method.getName().startsWith("get") && !method.getName().startsWith("in") && !method.getName().startsWith("is")) {//#549
            return false;
        }
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        return !void.class.equals(method.getReturnType());
    }

    public Long getAppPk() {
        return accountPk;
    }

    public void setAppPk(Long appPk) {
        this.accountPk = appPk;
    }

    public Long getBorrowerPk() {
        return clientPk;
    }

    public void setBorrowerPk(Long borrowerPk) {
        this.clientPk = borrowerPk;
    }

    @Override
    public String toString() {
        return "ChangedObjectGent2{" + "entity=" + entity + ", id=" + id + ", currentState=" + Arrays.toString(currentState) + ", previousState=" + Arrays.toString(previousState) + ", propertyNames=" + Arrays.toString(propertyNames) + ", types=" + Arrays.toString(types) + ", type=" + changeType + '}';
    }

}
