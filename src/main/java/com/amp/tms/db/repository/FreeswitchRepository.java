/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.db.repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author HS
 */
@Repository
@Transactional
public class FreeswitchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean isOnCallFreeswitch(int ext) {
        Number results;
        try {
            results = (Number) entityManager.createNativeQuery("select count(*) from tms.channels chan where (chan.dest = :extStr or chan.cid_name = :extStr)")
                    //                    .setParameter("ext", ext)
                    .setParameter("extStr", Integer.toString(ext))
                    .getSingleResult();
        } catch (NoResultException ex) {
            return false;
        }
        return results.longValue() > 0;
    }

    public boolean isRegisteredOnFreeswitch(int ext) {
        Number results;
        try {
            results = (Number) entityManager.createNativeQuery("select count(*) from tms.registrations reg where (reg.reg_user = :extStr)")
                    //                    .setParameter("ext", ext)
                    .setParameter("extStr", Integer.toString(ext))
                    .getSingleResult();
            return results.longValue() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isRegisteredOnFreeswitchSip(int ext) {
        Number results;
        try {
            results = (Number) entityManager.createNativeQuery("select count(*) from tms.sip_registrations reg where (reg.sip_user = :extStr) and ping_status='Reachable'")
                    //                    .setParameter("ext", ext)
                    .setParameter("extStr", Integer.toString(ext))
                    .getSingleResult();
            return results.longValue() > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    public String getOtherChannalBForChannalA(String uuid) {
        String results;
        try {
            results = (String) entityManager.createNativeQuery("select uuid from tms.channels where  call_uuid <> uuid and call_uuid = :callUUID")
                    .setParameter("callUUID", uuid)
                    .getSingleResult();
            return results;
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean callUUIDExistsOnFreeswitch(String uuid) {
        Number results;
        try {
            results = (Number) entityManager.createNativeQuery("select count(*) from tms.channels where uuid = :callUUID")
                    .setParameter("callUUID", uuid)
                    .getSingleResult();
            return results.longValue() > 0;
        } catch (Exception ex) {
            return false;
        }

    }

}
