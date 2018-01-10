/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.configurations;

import com.amp.crm.constants.WorkLogConstants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.task.TaskExecutor;

public class AuditInterceptor extends EmptyInterceptor {

    private static String sessionIdLogNote = "[Uknown session]";

    private long group;

    private long transactionId;
    private long threadId;
    private TaskExecutor executor;
    
    private int updates;
    private int inserts;
    private int selects;
    private int deletes;

    private AuditEntityManager auditEntityManager;
    private LinkedHashMap<Integer, ChangedEntity> changedObjects;
    private static Logger LOG = LoggerFactory.getLogger(AuditInterceptor.class);

    /**
     * author hsleiman
     *
     * @param entity
     * @param id
     * @param state
     * @param propertyNames
     * @param types Add to the list of changedObjects with their state and operationType as DELETE.
     */
    @Override
    public void onDelete(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {
        deletes++;
        if (isActiveClass(entity.getClass().getSimpleName())) {
            changedObjects.put(entity.hashCode(), new ChangedEntity(entity, id, state, propertyNames, types, WorkLogConstants.DATA_CHANGE_LOG_DELETE));
        }
    }

    /**
     * author hsleiman
     *
     * @param entity
     * @param id
     * @param state
     * @param propertyNames
     * @param types
     * @return
     */
    @Override
    public boolean onLoad(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {

        selects++;
        return super.onLoad(entity, id, state, propertyNames, types);
    }

    /**
     * author hsleiman
     *
     * @param entity
     * @param id
     * @param currentState
     * @param previousState
     * @param propertyNames
     * @param types
     * @return
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        updates++;
        //System.out.println("is entity:"+entity.getClass().getSimpleName()+" active?***"+isActiveClass(entity.getClass().getSimpleName()));
        if (isActiveClass(entity.getClass().getSimpleName())) {
            ChangedEntity changedObject = changedObjects.get(entity.hashCode());
            if (changedObjects.get(entity.hashCode()) != null) {
                previousState = changedObjects.get(entity.hashCode()).getPreviousState();
            }
            if (changedObject != null && changedObject.getChangeType() == WorkLogConstants.DATA_CHANGE_LOG_CREATE) {
                changedObjects.put(entity.hashCode(), new ChangedEntity(entity, id, currentState, propertyNames, types, WorkLogConstants.DATA_CHANGE_LOG_CREATE));
            } else {
                changedObjects.put(entity.hashCode(), new ChangedEntity(entity, id, currentState, previousState, propertyNames, types));
            }
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    /**
     * author hsleiman
     *
     * @param entity
     * @param id
     * @param state
     * @param propertyNames
     * @param types
     * @return
     */
    @Override
    public boolean onSave(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {

        inserts++;
        //System.out.println("is entity:"+entity.getClass().getSimpleName()+" active?***"+isActiveClass(entity.getClass().getSimpleName()));
        if (isActiveClass(entity.getClass().getSimpleName())) {
            changedObjects.put(entity.hashCode(), new ChangedEntity(entity, id, state, propertyNames, types, WorkLogConstants.DATA_CHANGE_LOG_CREATE));
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }

    /**
     * author hsleiman
     *
     * @param tx
     */
    @Override
    public void afterTransactionBegin(Transaction tx) {
        group = System.currentTimeMillis();

        changedObjects = new LinkedHashMap<>();
        auditEntityManager = (AuditEntityManager) ApplicationContextProvider.getApplicationContext().getBean(AuditEntityManager.class);
        executor = ApplicationContextProvider.getApplicationContext().getBean("los-executor", TaskExecutor.class);

        ThreadAttributes.setString("current.transaction.id", tx.hashCode() + "");
        transactionId = tx.hashCode();

        ThreadAttributes.setString("current.thread.id", Thread.currentThread().getId() + "");
        threadId = Thread.currentThread().getId();

        sessionIdLogNote = "[[TID = " + ThreadAttributes.getString("current.thread.id") + "]" + " [TX = " + ThreadAttributes.getString("current.transaction.id") + "]] ";
        ThreadAttributes.setString("current.session.log.info", sessionIdLogNote);

        LOG.info("{}Transaction Begin [{}]", sessionIdLogNote, tx.hashCode());
        super.afterTransactionBegin(tx);
    }

    /**
     * author hsleiman
     *
     * @param tx
     */
    @Override
    public void beforeTransactionCompletion(Transaction tx) {
        long elapse = System.currentTimeMillis() - group;
        if (elapse > (1000 * 15)) {
            LOG.warn("{}Transaction About to Commit [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
        } else {
            LOG.info("{}Transaction About to Commit [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
        }
    }

    /**
     * author hsleiman
     *
     * @param tx
     */
    @Override
    public void afterTransactionCompletion(Transaction tx) {
        long elapse = System.currentTimeMillis() - group;
        if (tx.wasCommitted()) {
            if (elapse > (1000 * 15)) {
                LOG.warn("{}Transaction Commit [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
            } else {
                LOG.info("{}Transaction Commit [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
            }
            flush();
            ThreadAttributes.clear(false);
        }
        if (tx.wasRolledBack()) {
            if (elapse > (1000 * 15)) {
                LOG.warn("{}Transaction Rollback [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
            } else {
                LOG.info("{}Transaction Rollback [{}] [{}ms] with (Select: {}, Inserts: {}, Updates: {}, Deletes: {})", sessionIdLogNote, tx.hashCode(), elapse, selects, inserts, updates, deletes);
            }
            ThreadAttributes.clear(true);
        }

        updates = 0;
        inserts = 0;
        selects = 0;
        deletes = 0;
    }

    private boolean isActiveClass(String className) {
        return auditEntityManager.isActive(className);
    }

    /**
     * author hsleiman
     */
    private void flush() {
        LOG.info("{}Number of entity changed: {}", sessionIdLogNote, changedObjects.size());
        if (changedObjects.isEmpty()) {
            return;
        }
        ArrayList<ChangedEntity> changedObjectGen2s = new ArrayList<>();
        LOG.info("{}Flushing data log changes...", sessionIdLogNote);
        Set<Integer> s = changedObjects.keySet();
        for (Integer integer : s) {
            ChangedEntity changedObject = changedObjects.get(integer);
            LOG.info("{}[flush] ChangedObject {}", sessionIdLogNote, changedObject.getClassName());
            changedObjectGen2s.add(changedObject);
        }
        LOG.info("{}Executing executer on changed objects", sessionIdLogNote);
        AutidInterceptorWorker workerGen2 = new AutidInterceptorWorker(changedObjectGen2s);
        AutowireCapableBeanFactory factory = ApplicationContextProvider.getApplicationContext().getAutowireCapableBeanFactory();
        factory.autowireBean(workerGen2);
        executor.execute((Runnable) factory.initializeBean(workerGen2, "workerGen2"));
        LOG.info("{}Executed executer on changed objects", sessionIdLogNote);

    }
}
