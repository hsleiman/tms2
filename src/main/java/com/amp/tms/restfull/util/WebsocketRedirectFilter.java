/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull.util;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.Member;
import com.hazelcast.core.PartitionService;
import com.objectbrains.hcms.hazelcast.HazelcastService;
import com.objectbrains.hcms.spring.HcmsConfiguration;
import java.io.IOException;
import java.net.URL;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * 
 */
public class WebsocketRedirectFilter extends OncePerRequestFilter {

    private Member localMember;
    private PartitionService partitionService;

    @Override
    protected void initFilterBean() throws ServletException {
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        HazelcastService hazelcast = context.getBean(HcmsConfiguration.LOCAL_HAZELCAST_SERVICE_BEAN_NAME, HazelcastService.class);
        Cluster cluster = hazelcast.getCluster();
        localMember = cluster.getLocalMember();
        partitionService = hazelcast.getPartitionService();
    }

    private Member getResponsibleMember(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String websocketPrefix = "/tms/websocket/websocket-e/";
        if (requestURI.startsWith(websocketPrefix)) {
            int end = requestURI.indexOf('/', websocketPrefix.length());
            if (end != -1) {
                String extStr = requestURI.substring(websocketPrefix.length(), end);
                try {
                    int ext = Integer.parseInt(extStr);
                    return partitionService.getPartition(ext).getOwner();
                } catch (NumberFormatException ex) {
                    //do nothing
                    return null;
                }
            }
        }
        return localMember;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Member member = getResponsibleMember(request);
        if (member == null) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!member.localMember()) {
            URL url = new URL(request.getScheme(),
                    member.getSocketAddress().getAddress().getHostAddress(),
                    request.getLocalPort(),
                    request.getRequestURI());
            response.sendRedirect(url.toString());
            return;
        }
        filterChain.doFilter(request, response);
    }

}
