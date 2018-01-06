/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.iws.restful.config;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Controller
public class RootController implements ServletContextAware{
    private ServletContext servletContext;
    
    @RequestMapping("/")
    public String root(){
        return "redirect:/app/";
    }
    
    @RequestMapping("/app/**")
    public String app(){
        return "forward:/index.html";
    }
    
    @RequestMapping(value="/AgentServiceIWS")
    public void agentServiceIWS(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        servletContext.getNamedDispatcher("CXFServlet").forward(request, response);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
