/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.iws.restful.filter;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.amp.crm.db.entity.utility.RestfullCallLog;
import com.amp.crm.db.repository.utility.RestfullCallRepository;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author HS
 */
@Component("userAuthFilter")
public class UserAuthFilter extends OncePerRequestFilter {

    @ConfigContext
    private AuthFilterConfig authFilterConfig;

    @Autowired
    private RestfullCallRepository restfullCallRepository;

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long elaps = System.currentTimeMillis();
        String url = request.getRequestURL().toString();
        String username = null;
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String nextElement = enumeration.nextElement();
            LOG.debug("header: {} - {}", nextElement, request.getHeader(nextElement));
            if (nextElement.equalsIgnoreCase("username")) {
                username = request.getHeader(nextElement);
            }
        }

        String remoteIpAddress = authFilterConfig.getIpAddressOfRequest(request);

        response.addHeader("Access-Control-Allow-Origin", authFilterConfig.getAccessControlAllowOrigin());
        response.addHeader("Access-Control-Allow-Methods", authFilterConfig.getAccessControlAllowMethods());
        response.addHeader("Access-Control-Max-Age", authFilterConfig.getAccessControlMaxAge());
        response.addHeader("Access-Control-Allow-Headers", authFilterConfig.getAccessControlAllowAHeaders());

        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            elaps = System.currentTimeMillis() - elaps;
            RestfullCallLog restfullCallLog = new RestfullCallLog();
            restfullCallLog.setElapseTime(elaps);
            restfullCallLog.setUrl(url);
            restfullCallLog.setUsername(username);
            restfullCallLog.setRemoteIpAddress(remoteIpAddress);
            restfullCallLog.setHttpMethod(request.getMethod());
            restfullCallLog.setHttpReponseCode(response.getStatus());
            restfullCallRepository.createLog(restfullCallLog);
            return;
        }
        filterChain.doFilter(request, response);
        
        elaps = System.currentTimeMillis() - elaps;
        
        LOG.info("url: {} took {}ms", url, elaps);
        
        RestfullCallLog restfullCallLog = new RestfullCallLog();
        restfullCallLog.setElapseTime(elaps);
        restfullCallLog.setUrl(url);
        restfullCallLog.setUsername(username);
        restfullCallLog.setRemoteIpAddress(remoteIpAddress);
        restfullCallLog.setHttpMethod(request.getMethod());
        restfullCallLog.setHttpReponseCode(response.getStatus());
        restfullCallRepository.createLog(restfullCallLog);
    }

}
