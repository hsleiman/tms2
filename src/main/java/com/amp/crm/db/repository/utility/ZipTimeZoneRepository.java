/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.repository.utility;

import com.amp.crm.db.entity.utility.ZipTimezone;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Repository
public class ZipTimeZoneRepository {
    
       
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<ZipTimezone> getLocationInfoByZip(String zip){
        TypedQuery<ZipTimezone> query = entityManager.createNamedQuery("ZipTimezone.GetRecordsByZip", ZipTimezone.class);
        query.setParameter("Zip", zip);
        return query.getResultList();
    }
    
    public List<ZipTimezone> getLocationInfoByAreaCode(Integer areaCode){
        TypedQuery<ZipTimezone> query = entityManager.createNamedQuery("ZipTimezone.getRecordsByAreaCode", ZipTimezone.class);
        query.setParameter("AreaCode", areaCode);
        return query.getResultList();
    }
    
    public List<ZipTimezone> getLocationInfoByCity(String city){
        TypedQuery<ZipTimezone> query = entityManager.createNamedQuery("ZipTimezone.getRecordsByCity", ZipTimezone.class);
        query.setParameter("City", city);
        return query.getResultList();
    }
    
    public void truncate(){
        entityManager.createNativeQuery("truncate svc.sv_zip_timezone").executeUpdate();
        entityManager.flush();
    }
    
    public void saveZipTimezone(ZipTimezone zipTimezone){
        entityManager.persist(zipTimezone);
    }
    
}
