/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.restfull.util;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author connorpetty
 */
public class CORSFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CORSFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        long time = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            time = System.currentTimeMillis() - time;
            LOG.info("URL Request: {}, time(ms): {}", request.getRequestURI(), time);
        }
    }

}
