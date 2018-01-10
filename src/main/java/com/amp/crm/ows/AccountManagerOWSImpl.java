/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.ows;

import com.objectbrains.ams.iws.AccountManagerIWS;
import com.objectbrains.ams.iws.Status;
import com.objectbrains.ams.iws.User;
import com.objectbrains.ams.iws.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class AccountManagerOWSImpl implements AccountManagerOWS {

    private static final Logger LOG = LoggerFactory.getLogger(AccountManagerOWSImpl.class);

    @Autowired
    private AccountManagerIWS amsService;

    private AmsUser createAmsUser(User user) {
        AmsUser amsUser = new AmsUser();
        amsUser.setUserName(user.getUserName());
        amsUser.setExtension(user.getExtension());
        amsUser.setFirstName(user.getFirstName());
        amsUser.setLastName(user.getLastName());
        amsUser.setEmailAddress(user.getEmailAddress());
        amsUser.setPhoneNumber(user.getPhoneNumber());
        amsUser.setLastAccessTime(user.getLastAccessTime());
        amsUser.setEffectiveCallerId(user.getEffectiveCallerId());
        amsUser.setActive(user.getStatus() == Status.ACTIVE);
        amsUser.setStatus(Status.ACTIVE);
        return amsUser;
    }

    @Override
    public AmsUser getUser(String username) {
        try {
            return createAmsUser(amsService.getUser(username));
        } catch (UserNotFoundException ex) {
            return null;
        }
    }

    @Override
    public AmsUser getUser(Integer extension) {
        try {
            return createAmsUser(amsService.getPhoneUser(extension));
        } catch (UserNotFoundException ex) {
            return null;
        }
    }

}
