/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.auth;

import com.objectbrains.ams.iws.AccountManagerIWS;
import com.objectbrains.ams.iws.LoginUserResult;
import com.objectbrains.ams.iws.User;
import com.objectbrains.ams.iws.UserNotFoundException;
import com.objectbrains.sti.pojo.user.AuthUserPojo;
import com.objectbrains.sti.service.auth.UserAuth;
import java.util.Enumeration;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@RestController()
@RequestMapping("/auth")
public class UserAuthentication {

    @Autowired
    private AccountManagerIWS accountManagerIWS;

    @Autowired
    private UserAuth userAuth;

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthentication.class);

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity login(@RequestHeader(value = "username") String username, @RequestHeader(value = "password") String password, HttpServletRequest request, HttpServletResponse response) {

        String ipAddress = "";
        Enumeration<String> ipAddresses = request.getHeaders("HTTP_X_FORWARDED_FOR"); //Ex: HTTP_X_FORWARDED_FOR - Can return multiple ip addresses seperated by commas
        if (!ipAddresses.hasMoreElements()) {
            ipAddresses = request.getHeaders("X_FORWARDED_FOR");  //check the url without http
        }
        while (ipAddresses.hasMoreElements()) { //HTTP_X_FORWARDED_FOR returns more 1 ip addresses, uses the last proxy id address
            ipAddress = ipAddresses.nextElement();
        }
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (username == null || password == null) {
            return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
        }
        User user;
        try {
            user = accountManagerIWS.getUser(username);
            if (user == null) {
                return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
        }
        LoginUserResult loginUserResult = accountManagerIWS.loginUser(1, username, password, ipAddress);

        if (loginUserResult == null) {
            return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
        } else {
            String token = UUID.randomUUID().toString();
            userAuth.addTokenForUser(username, token, loginUserResult.getPermissions());
            AuthUserPojo aup = new AuthUserPojo();
            aup.setTokenKey(token);
            return new ResponseEntity(aup, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ResponseEntity logout(@RequestHeader(value = "username") String username, @RequestHeader(value = "tokenkey") String tokenkey, HttpServletRequest request, HttpServletResponse response) throws UserNotFoundException {

        if (userAuth.isValid(username, tokenkey)) {
            accountManagerIWS.updateLastAccessTime(username);
            userAuth.clearTokenForUser(username);
        }
        return new ResponseEntity("{\"msg\":\"success\"}", HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/validsession", method = RequestMethod.GET)
    public ResponseEntity validsession(@RequestHeader(value = "username") String username, HttpServletRequest request, HttpServletResponse response) throws UserNotFoundException {

        if (userAuth.userLoggedIn(username)) {
            return new ResponseEntity("{\"auth\":true}", HttpStatus.OK);
        }
        return new ResponseEntity("{\"auth\":false}", HttpStatus.OK);
    }

    @RequestMapping(value = "/validtoken", method = RequestMethod.POST)
    public ResponseEntity validtoken(@RequestHeader(value = "username") String username, @RequestHeader(value = "tokenkey") String tokenkey, HttpServletRequest request, HttpServletResponse response) throws UserNotFoundException {

        if (userAuth.isValid(username, tokenkey)) {
            return new ResponseEntity("{\"auth\":true}", HttpStatus.OK);
        }
        return new ResponseEntity("{\"auth\":false}", HttpStatus.OK);
    }

    @RequestMapping(value = "/getToken", method = RequestMethod.GET)
    public ResponseEntity getToken(@RequestHeader(value = "username") String username, @RequestHeader(value = "password") String password, HttpServletRequest request, HttpServletResponse response) throws UserNotFoundException {

        String ipAddress = "";
        Enumeration<String> ipAddresses = request.getHeaders("HTTP_X_FORWARDED_FOR"); //Ex: HTTP_X_FORWARDED_FOR - Can return multiple ip addresses seperated by commas
        if (!ipAddresses.hasMoreElements()) {
            ipAddresses = request.getHeaders("X_FORWARDED_FOR");  //check the url without http
        }
        while (ipAddresses.hasMoreElements()) { //HTTP_X_FORWARDED_FOR returns more 1 ip addresses, uses the last proxy id address
            ipAddress = ipAddresses.nextElement();
        }
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (username == null || password == null) {
            return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
        }
        if (userAuth.userLoggedIn(username)) {
            User user;
            try {
                user = accountManagerIWS.getUser(username);
                if (user == null) {
                    return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception ex) {
                return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
            }
            LoginUserResult loginUserResult = accountManagerIWS.loginUser(1, username, password, ipAddress);

            if (loginUserResult == null) {
                return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
            } else {
                String token = userAuth.getTokenForUsername(username);
                AuthUserPojo aup = new AuthUserPojo();
                aup.setTokenKey(token);
                return new ResponseEntity(aup, HttpStatus.OK);
            }
        }
        return new ResponseEntity("{\"msg\":\"Invalid username / password\"}", HttpStatus.UNAUTHORIZED);
    }

}
