/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.service.utility;

import com.objectbrains.sti.embeddable.OutboundDialerQueueRecord;
import com.objectbrains.sti.pojo.DialerQueueAccountDetails;
import com.objectbrains.sti.pojo.DialerQueueRecord;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 *
 * @author David
 */
public class HttpClient implements Closeable {

    private final HttpURLConnection connection;
    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    public HttpClient(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        if (url.getProtocol().equals("https")) {
            try {
                HostnameVerifier hv = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        LOG.info("getAcceptedIssuers =============");
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        LOG.info("checkClientTrusted =============");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        LOG.info("checkServerTrusted =============");
                    }
                }}, new SecureRandom());

                connection = (HttpsURLConnection) url.openConnection();
                HttpsURLConnection securedConn = (HttpsURLConnection) connection;
                securedConn.setHostnameVerifier(hv);
                securedConn.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public void setBasicAuthentication(String username, String password) {
        String userpassword = username + ":" + password;
        String encodedAuthorization = EncoderUtils.encodeBase64String(userpassword);
        connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
    }

    public String sendGetRequest(String urlString) throws MalformedURLException, IOException {
        String response = null;
        try {
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            //get response data
            LOG.info("Reading output... \n");
            response = getHttpResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public String sendPostRequest(String urlString, String input) throws MalformedURLException, IOException {
        String response = null;
        try {
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            //send request data
            LOG.info("Input: {}", input);
            if (input != null) {
                connection.setDoOutput(true);
                byte[] bytes = null;
                if (input != null) {
                    bytes = input.getBytes();
                }
                OutputStream os = connection.getOutputStream();
                os.write(bytes);
                os.flush();
            }
            //get response data
            LOG.info("Reading output... \n");
            response = getHttpResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static String sendPostRequestAsJSON(String url, Object input) throws IOException {
        return (String) sendPostRequestAsJSON(url, input, String.class);
    }
    
    public static <T> T sendPostRequestAsJSON(String url, Object input, Class<T> output) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String inputStr = JSONUtils.objectToJSON(input);
        inputStr = StringEscapeUtils.escapeJson(inputStr);
        String response = client.sendPostRequest(url, inputStr);
        return JSONUtils.JSONToObject(response, output);
    }
     
    public static String sendPostRequestAsJSON(String url, Object input, String username, String password) throws IOException {
        return (String) sendPostRequestAsJSON(url, input, String.class, username, password);
    }
    
    public static <T> T sendPostRequestAsJSON(String url, Object input, Class<T> output, String username, String password) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        client.setBasicAuthentication(username, password);
        String inputStr = JSONUtils.objectToJSON(input);
        inputStr = StringEscapeUtils.escapeJson(inputStr);
        String response = client.sendPostRequest(url, inputStr);
        return JSONUtils.JSONToObject(response, output);
    }

    public static <T> T sendGetRequestAsJSON(String url, Class<T> output, String username, String password) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
        client.setBasicAuthentication(username, password);
        String response = client.sendGetRequest(url);
        return JSONUtils.JSONToObject(response, output);
    }

    public static <T> T sendGetRequestAsJSON(String url, Class<T> output) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
        String response = client.sendGetRequest(url);
        return JSONUtils.JSONToObject(response, output);
    }

    public static <T> List<T> sendGetRequestAsJSONList(String url, Class<T> output) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
        String response = client.sendGetRequest(url);
        List<T> obj = JSONUtils.JSONToObjectList(response, output);
        return obj;
    }

    public static String sendGetRequestAsJSON(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
        client.setBasicAuthentication(username, password);
        String response = client.sendGetRequest(url);
        return response;
    }

    public static String sendPostRequestAsText(String url, String body) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.TEXT_PLAIN_VALUE);
        String response = client.sendPostRequest(url, body);
        return response;
    }
    
    public static String sendGetRequestAsText(String url) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.TEXT_PLAIN_VALUE);
        String response = client.sendGetRequest(url);
        return response;
    }

    private static String getHttpResponse(HttpURLConnection conn) throws IOException {
        String response = null;
        InputStream in;
        int statusCode = conn.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_OK) {
            in = conn.getInputStream();
        } else {
            in = conn.getErrorStream();
        }
        if (in != null) {
            //response = IOUtils.toString(in);
            response = readHttpResponse(in);
            LOG.info(response);
        }
        //System.out.println("response: " + conn.getResponseMessage());
        return response;
    }

    public static String readHttpResponse(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            OutboundDialerQueueRecord dqRecord = new OutboundDialerQueueRecord();
            dqRecord.setDqPk(1);
            List<DialerQueueAccountDetails> ld = dqRecord.getAccountDetails();
            DialerQueueAccountDetails ld1 = new DialerQueueAccountDetails();
            ld1.setAccountPk(1);
            ld.add(ld1);
            System.out.println("POST: " + sendPostRequestAsJSON("http://localhost:8080/tms/dq/update", dqRecord));
            System.out.println("GET: " + sendGetRequestAsJSON("http://localhost:8080/tms/dq/test1/", DialerQueueRecord.class));
        } catch (IOException ex) {
            LOG.info(ExceptionUtils.getStackTrace(ex));
        }
    }
}
