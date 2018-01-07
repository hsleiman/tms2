/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.utility;

import com.amp.crm.db.entity.base.SqlConfig;
import com.amp.crm.exception.TooManyObjectFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository
public class SQLConfigRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    EntityManager getEntityManager() {
        return entityManager;
    }

    public SqlConfig locateByPk(long configPk ) {
        List<SqlConfig> res = getEntityManager().createNamedQuery("SqlConfig.locateByPk", SqlConfig.class).
                setParameter("pk", configPk).getResultList();
        if ((res == null) || (res.isEmpty())) return null;
        if (res.size() > 1) throw new TooManyObjectFoundException("Found too many entries for a given pk");
        return res.get(0);
    }
    
    public SqlConfig locateBySqlName(String sqlName ) {
        TypedQuery<SqlConfig> query = getEntityManager().createNamedQuery("SqlConfig.getCurrentSQLByName", SqlConfig.class);
        List<SqlConfig> res = query.
                setParameter("sqlName", sqlName.toLowerCase()).getResultList();
        if ((res == null) || (res.isEmpty())) return null;
        if (res.size() > 1) throw new TooManyObjectFoundException("Found too many current entries for a given sqlName");
        return res.get(0);
    }
    
    public String getQueryBySqlName(String sqlName ) {
        TypedQuery<SqlConfig> query = getEntityManager().createNamedQuery("SqlConfig.getCurrentSQLByName", SqlConfig.class);
        List<SqlConfig> res = query.
                setParameter("sqlName", sqlName.toLowerCase()).getResultList();
        if ((res == null) || (res.isEmpty())) return "";
        if (res.size() > 1) throw new TooManyObjectFoundException("Found too many current entries for a given sqlName");
        return res.get(0).getSQLString();
    }
    
    public List<String> getAllActiveACHSqlNames() {
        TypedQuery<String> query = getEntityManager().createQuery(
                "SELECT s.sqlName FROM SqlConfig s WHERE s.category='ach' AND s.isActive=true", String.class);
        List<String> res = query.getResultList();
        if ((res == null) || (res.isEmpty())) return null;
        return res;
    }
    
    @SuppressWarnings("unchecked")
     public List<SqlConfig> locateAllBySqlNamePattern(String sqlNamePattern ) {
        Query query = getEntityManager().createNativeQuery("Select * from svc.sv_sql_config s where sql_name ~* '"+sqlNamePattern+"' AND s.is_active = 'Y' order by execute_order asc", SqlConfig.class);
        List<SqlConfig> res = query.getResultList();
        return res;
    }
     
      public List<SqlConfig> locateAllSqlsByGroupId(Integer groupId) {
        TypedQuery<SqlConfig> query = getEntityManager().createQuery("Select s from SqlConfig s where s.groupId = :groupId AND s.isActive = true ORDER by s.executeOrder asc", SqlConfig.class);
        List<SqlConfig> res = query.
                setParameter("groupId", groupId).getResultList();
        return res;
    }
    
    
}
