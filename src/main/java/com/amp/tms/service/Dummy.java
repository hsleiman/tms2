/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.amp.tms.db.repository.DialplanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Hoang, J, Bishistha
 */
@Service
public class Dummy {

    private static final Logger log = LoggerFactory.getLogger(Dummy.class);
    
    @Autowired 
    private DialplanRepository dialplanRepository;

    @Transactional(rollbackFor = Exception.class)
    public void dummy(String text) {
        
                try {
                    dialplanRepository.LogDialplanText(text);

                } catch (Exception ex) {

                    log.error(ex.getMessage(), ex);
                }
            
            try {
                dialplanRepository.LogDialplanText(text + "_1");
            } catch (Exception ex) {
               log.error(ex.getMessage(), ex);
            }

    }
}
