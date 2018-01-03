/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.objectbrains.ams.iws.AccountManagerIWS;
import com.objectbrains.ams.iws.FindUsersRequest;
import com.objectbrains.ams.iws.LoginActivity;
import com.objectbrains.ams.iws.Status;
import com.objectbrains.ams.iws.User;
import com.objectbrains.ams.iws.UserNotFoundException;
import com.objectbrains.scheduler.annotation.Sync;
import com.objectbrains.sti.embeddable.AgentWeightPriority;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
@Sync
public class AmsService {

    private static final Logger LOG = LoggerFactory.getLogger(AmsService.class);

    @Autowired
    private AccountManagerIWS amsIWS;

    public User getUser(String userName) {
        try {
            return amsIWS.getPhoneUser2(userName);
        } catch (UserNotFoundException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    public List<User> getUsers(List<AgentWeightPriority> awpList) {
        List<User> users = new ArrayList<>();
        for (AgentWeightPriority awp : awpList) {
            try {
                users.add(amsIWS.getPhoneUser2(awp.getUsername()));
            } catch (UserNotFoundException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return users;
    }

    public User getUser(Integer extension) {
        if (extension > 1000 && extension < 10000) {
            try {
                return amsIWS.getPhoneUser(extension);
            } catch (UserNotFoundException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return null;
    }

    public LoginActivity getLastLoginActivity(String userName) {
        try {
            return amsIWS.getLastLoginActivity(userName);
        } catch (UserNotFoundException ex) {
            LOG.error(ex.getMessage());
            return null;
        }
    }

    public List<User> getAllUsers() {
        FindUsersRequest findUsersRequest = new FindUsersRequest();
        findUsersRequest.setStatus(Status.ACTIVE);
        return amsIWS.findUsers(findUsersRequest);
    }

//    @RunOnce
//    private void initialOnTMSLoad() {
//        getUser("");
//    }
}
