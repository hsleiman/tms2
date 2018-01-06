package com.amp.tms.service;

import com.objectbrains.config.GoogleAPIConfigs;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.tms.utility.HttpClient;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import javax.annotation.PostConstruct;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class GCESignedUtility {

    private static final Logger LOG = LoggerFactory.getLogger(GCESignedUtility.class);

    private String SERVICE_ACCOUNT_EMAIL = "370136647692-e8r7p6e3epdgsbt523fccej9n67tccfj@developer.gserviceaccount.com";

    private String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "//sw/cert/GoogleApiKey.p12";

    private PrivateKey KEY;

    @ConfigContext
    private GoogleAPIConfigs googleAPIConfigs;

    @ConfigContext
    private ConfigurationUtility config;

    @PostConstruct
    public void init() {
        try {
            KEY = loadKeyFromPkcs12(SERVICE_ACCOUNT_PKCS12_FILE_PATH, "notasecret".toCharArray());
            SERVICE_ACCOUNT_PKCS12_FILE_PATH = googleAPIConfigs.getGOOGLE_SERVICE_ACCOUNT_PKCS12_FILE_PATH();
            SERVICE_ACCOUNT_EMAIL = googleAPIConfigs.getGOOGLE_SERVICE_ACCOUNT_EMAIL();

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public String GetCallRecordingURL(String filePath) throws Exception {
        String bucketName = config.getString("tms.gce.phone.recording.root.bucket", FreeswitchConfiguration.getPhoneRecordingBucket());
        return getSigningURL("GET", filePath, bucketName);
    }

    public String GetCallScreenRecordingURL(String filePath) throws Exception {
        String bucketName = config.getString("tms.gce.screen.recording.root.bucket", FreeswitchConfiguration.getPhoneRecordingBucket());
        return getSigningURL("GET", filePath, bucketName);
    }

//    public String UploadCallScreenRecordingURL(String filePath){
//        
//    }
    private String getSigningURL(String verb, String filePath, String bucketName) throws Exception {
        
        long expiration = System.currentTimeMillis() / 1000 + config.getInteger("tms.gce.cloud.storage.timelimit", 600);
        String url_signature = this.signString(verb + "\n\n\n" + expiration + "\n" + "/" + bucketName + "/" + filePath);
        String signed_url = "http"+config.getString("add.s.to.http.string", "")+"://storage.googleapis.com/" + bucketName + "/" + filePath
                + "?GoogleAccessId=" + SERVICE_ACCOUNT_EMAIL
                + "&Expires=" + expiration
                + "&Signature=" + URLEncoder.encode(url_signature, "UTF-8");
        return signed_url;
    }

    public static String GetGoogleProjectId() {
        String projectID = "";
        try {
            String url = "http://metadata/computeMetadata/v1/project/numeric-project-id";
            HttpClient client = new HttpClient(url);
            HttpURLConnection conn = client.getConnection();
            conn.setRequestProperty("Accept", MediaType.TEXT_HTML_VALUE);
            conn.setRequestProperty("Metadata-Flavor", "Google");
            projectID = client.sendGetRequest(url);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return projectID;
    }

    private static PrivateKey loadKeyFromPkcs12(String filename, char[] password) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(fis, password);
        return (PrivateKey) ks.getKey("privatekey", password);
    }

    private String signString(String stringToSign) throws Exception {
        if (KEY == null) {
            throw new Exception("Private Key not initalized");
        }
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(KEY);
        signer.update(stringToSign.getBytes("UTF-8"));
        byte[] rawSignature = signer.sign();
        return new String(Base64.encodeBase64(rawSignature, false), "UTF-8");
    }
}
