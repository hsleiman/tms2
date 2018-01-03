/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.tms.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.objectbrains.gce.GCEException;
import com.objectbrains.gce.GCEService;
import com.objectbrains.tms.db.entity.cdr.CallDetailRecord;
import com.objectbrains.tms.db.entity.cdr.SpeechToText;
import com.objectbrains.tms.db.repository.CallDetailRecordRepository;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.annotation.PostConstruct;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author connorpetty
 */
@Service
public class SpeechToTextService {

    @Autowired
    private CallDetailRecordRepository callDetailRecordRepository;

    @Autowired
    private CdrService cdrService;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    private static final Logger log = LoggerFactory.getLogger(SpeechToTextService.class);

    private GCEService gceService;

    @PostConstruct
    private void init() {
        String accountId = System.getProperty("tms.gce.accountid", "p1-docs@cashcall-consumer-1321.iam.gserviceaccount.com");
        String pksFilePath = System.getProperty("tms.gce.keypath", "//sw/cert/CashCall-Consumer-220537c1e9f0.p12");
        String gcePassword = System.getProperty("tms.gce.password", "notasecret");
        try {
            gceService = new GCEService(accountId, new File(pksFilePath).toURI().toURL(), gcePassword);
        } catch (GCEException | MalformedURLException ex) {
            log.warn("Failed to create a GCEService, will use gsutil as backup", ex);
        }
    }

    @Async
    public void processSpeechToText(String callUUID, Double confidence, Double confidenceRight, Double confidenceLeft, int count) {
        if (count > 2) {
            return;
        }
        try {
            log.info("Get Speech to text Transcript {} - Count {}", callUUID, count);

            CallDetailRecord mcdr = callDetailRecordService.getCDR(callUUID);
            String date = FreeswitchConfiguration.formatToYYYY_MM_DD(mcdr.getCreateTimestamp());

            String remotePath = date + "/" + callUUID + "/" + callUUID;

            SpeechToText toText = new SpeechToText();
            toText.setCall_uuid(callUUID);
            toText.setTimestamp(LocalDateTime.now());
            toText.setBothChannal(getString(FreeswitchConfiguration.getPhoneRecordingBucket(), remotePath + ".txt"));
            toText.setRightChannal(getString(FreeswitchConfiguration.getPhoneRecordingBucket(), remotePath + "-right.txt"));
            toText.setLeftChannal(getString(FreeswitchConfiguration.getPhoneRecordingBucket(), remotePath + "-left.txt"));

            log.info("Saving Recoding text {}", remotePath);
            callDetailRecordRepository.persist(toText);

            callDetailRecordService.updateSpeechToTextCompleted(callUUID, Boolean.TRUE, confidence, confidenceRight, confidenceLeft);
            mcdr.setSpeechToTextCompleted(Boolean.TRUE);
            mcdr.setSpeechToTextConfidence(confidence);
            mcdr.setSpeechToTextConfidenceRight(confidenceRight);
            mcdr.setSpeechToTextConfidenceLeft(confidenceLeft);
           
            log.info("Both \n{}", toText.getBothChannal());
            log.info("Right \n{}", toText.getRightChannal());
            log.info("Left \n{}", toText.getLeftChannal());

        } catch (Exception ex) {
            log.error("Exception {}", ex);
            try {
                Thread.sleep(5000l);
            } catch (InterruptedException ex1) {

            }
            count++;
            processSpeechToText(callUUID, confidence, confidenceRight, confidenceLeft, count);
        }

    }

    @Async
    public void processSpeechToTextError(String callUUID) {
        log.info("Get Speech to text Error {}", callUUID);
        callDetailRecordService.updateSpeechToTextError(callUUID, Boolean.TRUE);
    }

    private String getString(String bucketName, String remotePath) throws FileNotFoundException, IOException {
        Storage storage = gceService.getStorage();
        log.info("Getting Recoding text {} - {}", bucketName, remotePath);
        Blob blob = storage.get(BlobId.of(bucketName, remotePath));
        byte[] f = blob.content();
        log.info("Got Recoding text {} size: {}", remotePath, f.length);
        return new String(f);
    }

}
