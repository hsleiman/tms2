/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.google.cloud.storage.BlobInfo;
import com.objectbrains.gce.GCEException;
import com.objectbrains.gce.GCEService;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.db.entity.base.dialer.BIMessage;
import com.amp.crm.embeddable.BIPlaybackData;
import com.amp.crm.service.tms.BIMessageService;
import static com.amp.tms.constants.Constants.SCREENSHOTS_PATH;
import com.amp.tms.db.entity.cdr.CallDetailRecordTMS;
import com.amp.tms.websocket.message.BiMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.common.util.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 *
 * 
 */
@Service
public class BiStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(BiStoreService.class);

    @Autowired
    private FreeswitchConfiguration configuration;

    @Autowired
    private CallDetailRecordService callDetailRecordService;

    @Autowired
    private BIMessageService bIMessageService;

    @Autowired
    @Qualifier("tms-executor")
    private TaskExecutor executor;

    @ConfigContext
    private ConfigurationUtility config;

    private GCEService gceService;

    @PostConstruct
    private void init() {
        String accountId = System.getProperty("tms.gce.accountid", "p1-docs@cashcall-consumer-1321.iam.gserviceaccount.com");
        String pksFilePath = System.getProperty("tms.gce.keypath", "//sw/cert/CashCall-Consumer-220537c1e9f0.p12");
        String gcePassword = System.getProperty("tms.gce.password", "notasecret");
        try {
            gceService = new GCEService(accountId, new File(pksFilePath).toURI().toURL(), gcePassword);
        } catch (GCEException | MalformedURLException ex) {
            LOG.warn("Failed to create a GCEService, will use gsutil as backup", ex);
        }
    }

    public void saveBiMessage(Integer extension, BiMessage data, String callUUID) {

        BIMessage message = new BIMessage();
        message.setCallUUID(callUUID);

//        AgentCall call = agentCallService.getActiveCall(extension);
//       if (callUUID == null && call != null) {
//           message.setCallUUID(call.getCallUUID());
//           message.setCallLoanPk(call.getBorrowerInfo().getLoanId());
//       }else{
        if (callUUID != null && callUUID.equals("") == false) {
            CallDetailRecordTMS cdr = callDetailRecordService.getCDR(callUUID);
            message.setCallLoanPk(cdr.getBorrowerInfo().getLoanId());
        }
        //       }

        if (message.getCallUUID() == null) {
            return;
        }

        message.setExtension(extension);
        if (data != null) {
            try {
                message.setUrlLoanPk(Long.parseLong(data.getLoanFromBi()));
            } catch (NumberFormatException ex) {
                LOG.info("Could not parse the Loan ID from BI the value was ({}) the URL was ({}).", data.getLoanFromBi(), data.getUrl());
                Pattern r = Pattern.compile("fe-sp2.+\\/(\\d+)");
                if (data.getUrl() != null) {
                    Matcher m = r.matcher(data.getUrl());
                    if (m.find()) {
                        String subUrl = m.group(0);
                        if (subUrl != null) {
                            r = Pattern.compile("(\\d{2}\\d+)");
                            m = r.matcher(subUrl);
                            if (m.find()) {
                                try {
                                    message.setUrlLoanPk(Long.parseLong(m.group(0)));
                                } catch (NumberFormatException exx) {
                                    LOG.info("Could not Parse the Loan ID from the URL was ({}).", data.getUrl());
                                }
                            }
                        }
                    }
                }
            }
            message.setDelay(data.getDelay());
            message.setEvent(data.getEvent());
            message.setFlagged(data.getFlagged());
            message.setBiAccountPk(data.getLoanFromBi());
            message.setPtp(data.getPtp());
//            message.setTimestamp(data.getTimeStamp());
            message.setUrl(data.getUrl());

            BiMessage.ToElement toElement = data.getToElement();
            if (toElement != null) {
                message.setElementType(toElement.getElementType());
                message.setNodeName(toElement.getNodeName());
                message.setNodeValue(toElement.getNodeValue());
            }

            String imgData = data.getImg();
            //save without the image since we will fill it in later
            Long now = System.currentTimeMillis();
            LocalDateTime nowLDT = new LocalDateTime(now);

            if (!StringUtils.isEmpty(imgData)) {
                String[] parts = imgData.split("(:|;|,)");
                MimeType mimeType = MimeTypeUtils.parseMimeType(parts[1]);
                final String rawData = parts[3];
                String fileExt = mimeType.getSubtype();

                final String screenRecordingForCall;
                if (message.getCallUUID() != null) {
                    screenRecordingForCall = FreeswitchConfiguration.formatToYYYY_MM_DD(LocalDateTime.now()) + "/" + message.getCallUUID() + "/" + now + "." + fileExt;
                } else {
                    screenRecordingForCall = null;
                }

                final String screenRecordingForExt;
                
                if (config.getBoolean("record.ext.all.the.time", true)) {
                    
                    String extExclude = config.getString("exclude.this.ext.for.all.time.recording", "");
                    if(extExclude.contains("|"+extension+"|")){
                        screenRecordingForExt = null;
                    }
                    else{
                        screenRecordingForExt = FreeswitchConfiguration.formatToYYYY_MM_DD(LocalDateTime.now()) + "/" + extension + "/" + nowLDT.getHourOfDay() + "/" + extension + "_" + now + "." + fileExt;
                    }
                    
                }
                else{
                    screenRecordingForExt = null;
                }

                String urlForCall = "https://" + configuration.getLoadBalancerHostname() + "/tms/screenshots/" + screenRecordingForCall;

                message.setImgUrl(urlForCall);

                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            boolean useAPI = gceService != null && config.getBoolean("tms-gce-use-api-for-screen-upload", true);
                            LOG.info("Uploading image via API {}", useAPI);
                            if (useAPI) {
                                if (screenRecordingForCall != null) {
                                    LOG.info("Uploading image {} via API For Call", screenRecordingForCall);
                                    BlobInfo info = BlobInfo.builder(FreeswitchConfiguration.getPhoneRecordingBucket(), screenRecordingForCall).build();
                                    gceService.getStorage().create(info, DatatypeConverter.parseBase64Binary(rawData));
                                }

                                if (screenRecordingForExt != null) {
                                    LOG.info("Uploading image {} via API For Ext", screenRecordingForExt);
                                    BlobInfo info = BlobInfo.builder(FreeswitchConfiguration.getPhoneRecordingBucket(), screenRecordingForExt).build();
                                    gceService.getStorage().create(info, DatatypeConverter.parseBase64Binary(rawData));
                                }

                            } else if (config.getBoolean("tms-gce-use-gs-util-for-screen-upload", true)) {
                                LOG.info("Uploading image {} via gsUtil", screenRecordingForCall);
                                File file = new File(SCREENSHOTS_PATH + screenRecordingForCall);
                                FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(rawData));
                                String gceRecordingPath = " gs://" + FreeswitchConfiguration.getPhoneRecordingBucket() + "/" + screenRecordingForCall;
                                String bash = "gsutil -q cp " + SCREENSHOTS_PATH + screenRecordingForCall + " " + gceRecordingPath;
                                executeCommand(bash);
                            }
                        } catch (Exception ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    }

                    private String executeCommand(String command) {

                        StringBuilder output = new StringBuilder();

                        Process p;
                        try {
                            p = Runtime.getRuntime().exec(command);
                            p.waitFor();
                            BufferedReader reader
                                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

                            String line = "";
                            while ((line = reader.readLine()) != null) {
                                output.append(line + "\n");
                            }

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            output.append(e.getMessage());
                        }

                        return output.toString();

                    }

                });
            }
        }
        bIMessageService.saveBIMessage(message);
    }

    public BIPlaybackData getBiPlaybackData(String callUUID) {
        return bIMessageService.getBIPlaybackData(callUUID);

//        callUUID = callUUID.trim();
//        CallDetailRecordTMS record = cdrService.findCDR(callUUID);
//        BiPlaybackData data = new BiPlaybackData();
//        if (record != null && record.getEnd_time() != null) {
//            data.setCallLength(DurationUtils.getDuration(record.getStart_time(), record.getEnd_time()));
//            data.setPlaybackElements(biStoreRepository.getPlaybackElements(callUUID, record.getStart_time()));
//        }
//        return data;
    }

}
