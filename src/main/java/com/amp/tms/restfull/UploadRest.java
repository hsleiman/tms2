/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.restfull;

import com.amp.tms.utility.HttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
@Path("/upload")
public class UploadRest {
    
    private static Logger LOG = LoggerFactory.getLogger(UploadRest.class);
    
    private String importTemplate;
    
    @Autowired
    @Qualifier("tms-csvUpload")
    private TaskExecutor executor;
    
    @PostConstruct
    private void init() throws IOException {
        importTemplate = IOUtils.toString(ResourceUtils.getURL("classpath:/com/objectbrains/tms/template/ImportTemplate.xml"));
    }
    
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    
    private static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }
    
    private static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }
    
    private static List<String> parseLine(String cvsLine, char separators, char customQuote) {
        
        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }
        
        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }
        
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }
        
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;
        
        char[] chars = cvsLine.toCharArray();
        
        for (char ch : chars) {
            
            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }
                    
                }
            } else {
                if (ch == customQuote) {
                    
                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }
                    
                } else if (ch == separators) {
                    
                    result.add(curVal.toString());
                    
                    curVal = new StringBuilder();
                    startCollectChar = false;
                    
                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }
            
        }
        
        result.add(curVal.toString());
        
        return result;
    }
    
    @POST
    @Path("/csv")
    @Consumes(MediaType.WILDCARD)
    public Response uploadCsv(InputStream uploadedInputStream) throws IOException {
        
        String jvmParam = System.getProperty("company.id");
        if(jvmParam.equalsIgnoreCase("settleit") == false){
            return Response.status(200).entity("Import is disabled on this system.").build();
        }
        
        try (InputStreamReader istream = new InputStreamReader(uploadedInputStream);
                BufferedReader reader = new BufferedReader(istream)) {
            String headerLine = reader.readLine();
            LOG.info("Uploading CSV with headers: {}", headerLine);
            List<String> headers = parseLine(headerLine);
            for (int i = 0; i < headers.size(); i++) {
                headers.set(i, "[" + headers.get(i).toUpperCase().replace(' ', '_') + "]");
            }
            String line = null;
            while ((line = reader.readLine()) != null) {
                
                String template = importTemplate.replace("[TODAY_DATE_IN_LONG]", "" + DateTime.now().getMillis());
                List<String> data = parseLine(line);
                
                LOG.info("Parsed CSV for loan {}", data.get(0));
                for (int i = 0; i < data.size(); i++) {
                    String d = data.get(i);
                    if (d.isEmpty()) {
                        d = "null";
                    }
                    template = template.replace(headers.get(i), d);
                }
                uploadLoan(data.get(0), template);
            }
        }
        
        return Response.status(200).entity("Successfully uploaded").build();
    }
    
    
    private void uploadLoan(final String loanId, final String template){
        executor.execute(new Runnable(){

            @Override
            public void run() {
                String body = DatatypeConverter.printBase64Binary(template.getBytes());
                try {
                    LOG.info("Uploading loan {} to svc", loanId);
                    HttpClient.sendPostRequestAsText("http://localhost:7070/svc/restful/import/import-loan-xml", body);
//                HttpClient.sendPostRequestAsText("http://appx.objectbrains.com:7070/svc/restful/import/import-loan-xml", body);
                } catch (Throwable ex) {
                    LOG.error("Failed to upload loan {}", loanId, ex);
                }
            }
            
        });
    }
}
