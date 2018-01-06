/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Java client to consume REST web service using POST or GET method
 *
 * @author raine.cabal
 */
public class HttpClient implements Closeable {

    private final HttpURLConnection connection;
    private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    public HttpClient(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
    }

    public HttpClient(String urlString, String json, String contentType) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", contentType);
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public String sendGetRequest(String urlString) throws MalformedURLException, IOException {
        String response = null;
        try {
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            //get response data
            LOG.debug("Reading output... \n");
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
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            //send request data
            LOG.debug("Input: {}", input);
            OutputStream os = connection.getOutputStream();
            os.write(input.getBytes());
            os.flush();

            //get response data
            LOG.debug("Reading output... \n");
            response = getHttpResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }

    public static String sendPostRequestWithHeaders(String url, String body, HashMap<String,String> headers) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            conn.addRequestProperty(key, value);
        }
        String response = client.sendPostRequest(url, body);
        return response;
    }

    public static Object sendPostRequestAsJSON(String url, Object object) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String input = JSONUtils.objectToJSON(object);
        String response = client.sendPostRequest(url, input);
        return JSONUtils.JSONToObject(response, object.getClass());
    }

    public static String sendPostRequestAsText(String url, String body) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.TEXT_PLAIN_VALUE);
        String response = client.sendPostRequest(url, body);
        return response;
    }
    
    public static String sendPostRequestAsApplicationJson(String url, String body) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String response = client.sendPostRequest(url, body);
        return response;
    }

    public static Object sendGetRequestAsJSON(String url, Class output) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
        String response = client.sendGetRequest(url);
        return JSONUtils.JSONToObject(response, output);
    }

    public static String sendGetRequestAsText(String url) throws IOException {
        HttpClient client = new HttpClient(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN_VALUE);
        String response = client.sendGetRequest(url);
        return response;
    }

    public static String sendGetRequestAsJSON(String url) throws IOException {
        HttpClient client = new HttpClient(url);
        String response = client.sendGetRequest(url);
        HttpURLConnection conn = client.getConnection();
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);
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
            LOG.debug(response);
        }
        LOG.debug("response: {}", conn.getResponseMessage());
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
}
