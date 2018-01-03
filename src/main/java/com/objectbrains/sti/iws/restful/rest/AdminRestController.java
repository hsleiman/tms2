/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.rest;

import com.objectbrains.sti.aop.Authorization;
import com.objectbrains.sti.constants.Permission;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author hsleiman
 */
@RestController()
@RequestMapping(value = "/adminRestController", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminRestController {
    
    @RequestMapping(value = "/testException", method = GET)
    @Authorization(permission = Permission.None, noPermissionTo = "Not Authenticated.")
    public void testException() {
        String test = null;
        test.indexOf(1);
    }
    
    
    
}
