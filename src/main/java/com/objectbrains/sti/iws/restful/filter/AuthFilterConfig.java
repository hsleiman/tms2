/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.iws.restful.filter;

import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hsleiman
 */
public class AuthFilterConfig extends ConfigurationUtility {


    public AuthFilterConfig(HazelcastService service) {
        super(service);
    }
    
    public String getAccessControlAllowOrigin(){
        return getString("Access-Control-Allow-Origin", "*");
    }
    
    public String getAccessControlAllowAHeaders(){
        return getString("Access-Control-Allow-Headers",  "Origin, X-Requested-With, Content-Type, Accept, username, password, tokenkey");
    }
    
    public String getAccessControlMaxAge(){
        return getString("Access-Control-Max-Age", "3600");
    }
    
    public String getAccessControlAllowMethods(){
        return getString("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    }
    
    public String getIpAddressOfRequest(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        
        if(ipAddress != null){
            return ipAddress;
        }
        
        Enumeration<String> ipAddresses = request.getHeaders("HTTP_X_FORWARDED_FOR"); //Ex: HTTP_X_FORWARDED_FOR - Can return multiple ip addresses seperated by commas
        if (!ipAddresses.hasMoreElements()) {
            ipAddresses = request.getHeaders("x-forwarded-for");  //check the url without http
        }
        
        while (ipAddresses.hasMoreElements()) { //HTTP_X_FORWARDED_FOR returns more 1 ip addresses, uses the last proxy id address
            ipAddress = ipAddresses.nextElement();
        }
        
        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

}
