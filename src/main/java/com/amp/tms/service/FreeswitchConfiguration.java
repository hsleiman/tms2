/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import static com.amp.tms.constants.Constants.IVR_PATH;
import static com.amp.tms.constants.Constants.SOUND_PATH;
import com.amp.tms.enumerated.CallDirection;
import com.amp.tms.enumerated.FreeswitchContext;
import java.net.UnknownHostException;
import java.util.Random;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * 
 */
@Service
public class FreeswitchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FreeswitchConfiguration.class);

    @ConfigContext
    private ConfigurationUtility config;

    public String getLocalHostAddress() {
        String use = "127.0.0.1";
        try {
            use = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.error("UnknownHostException: " + ex.getMessage());

        }
        return use;
    }

    public Integer getCallWaitTimeoutBeforeConnect(CallDirection callDirection) {
        if (callDirection == null) {
            return config.getInteger("call.wait.timeout.before.connect", 30);
        }
        if (callDirection == CallDirection.OUTBOUND) {
            return config.getInteger("call.wait.timeout.before.connect.OUTBOUND", 80);
        }
        return config.getInteger("call.wait.timeout.before.connect." + callDirection.name(), config.getInteger("call.wait.timeout.before.connect", 30));
    }

    public String getLocalHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return getLocalHostAddress();
        }
    }

    public boolean getDbLoging() {
        return config.getBoolean("get.database.loging.enabled", true);
    }

    public boolean getDbLogingForDialer() {
        return config.getBoolean("get.database.loging.for.dialer.enabled", true);
    }

    public boolean getTMSDialplanMapAsync() {
        return config.getBoolean("get.tms.dialplan.map.async", true);
    }

    public boolean getTMSDialplanMapAsync1() {
        return config.getBoolean("get.tms.dialplan.map.async1", true);
    }

    public boolean getIsStandAloneServer() {
        String value = System.getProperty("tmsStandaloneServer");
        if (value != null && value.equalsIgnoreCase("TRUE")) {
            return true;
        }
        return false;
    }

    public Integer getRingingTimeoutDelta() {
        return config.getInteger("ringing.timeout.delta", 10);
    }

    public boolean enableHasMutlipleMatchesForInboundCallingCheck() {
        return config.getBoolean("enable.has.multiple.matches.for.inbound.calling.check", true);
    }

    public boolean enableFreeswitchSwitchFSAgentServiceCanRecieveCallCheck() {
        return config.getBoolean("enable.freeswitch.switch.FS.agent.service.can.recieve.call", true);
    }

    public boolean enableFreeswitchSwitchFSAgentServiceAgentCallUpdate() {
        return config.getBoolean("enable.freeswitch.switch.FS.agent.service.agent.call.update", true);
    }

    public boolean enableFreeswitchSwitchFSAgentServiceAgentCallUpdate1() {
        return config.getBoolean("enable.freeswitch.switch.FS.agent.service.agent.call.update.1", false);
    }

    public boolean useNewFreeswitchDirectoryLookUp() {
        return config.getBoolean("use.new.freeswitch.directory.lookup", true);
    }

    public boolean useNewFreeswitchDirectoryLookUpAllowEmptyPassword() {
        return config.getBoolean("use.new.freeswitch.directory.lookup.allow.empty.password", false);
    }

    public boolean enableFreeswitchSwitchFSAgentServiceAgentCallUpdate2() {
        return config.getBoolean("enable.freeswitch.switch.FS.agent.service.agent.call.update.2", false);
    }

    public String getPhoneRecordingLocationURL() {
        return config.getString("tms.freeswitch.phone.recording.location.URL", "https://" + getLoadBalancerHostname() + "/tms/recording/");
    }

    public String getLoadBalancerHostname() {
        String other = config.getString("tms.load.balancer.location.URL", null);
        if (other == null) {
            other = getPrimaryTMSServer();
        }
        if (config.getBoolean("tms.replace.dash.with.dots.in.hostname", false)) {
            other = other.replaceAll("-", ".");
        }
        return other;
    }

    public String getPrimaryTMSServer() {
        String other = config.getString("tms.primary.tms.server", null);
        if (other == null) {
            other = getLocalHostName();
        }
        return other;
    }

    private String getFreeswitchIP() {
        String freeswitchIP;
        log.info("Locating  up freeswitch ip: {}", getLocalHostAddress());
        freeswitchIP = config.getString("tms.connnect.to.freeswitch.ip." + getLocalHostAddress(), getLocalHostAddress());
        log.info("Locating  up freeswitch ip: {} -> {}", getLocalHostAddress(), freeswitchIP);
        return freeswitchIP;
    }

    public String getExtensionConfig(String str) {
        return config.getString("extension.config." + str, "");
    }

    public Boolean useTransferForIncomingDialerOrder() {
        return config.getBoolean("use.transfer.for.incoming.dialer.order", Boolean.FALSE);
    }

    public boolean isScreenRecording() {
        return config.getBoolean("enable.screen.recording.backup.upload", false);
    }

    public boolean useFreeswitchLoadBalancer() {
        return config.getBoolean("use.loadbalancer.for.freeswitch", false);
    }

    public boolean useFreeswitchLoadBalancerPerContext() {
        return config.getBoolean("use.loadbalancer.for.freeswitch.per.context", false);
    }

    public String getFreeswitchLoadBalancerPerContextIP(FreeswitchContext context) {
        return config.getString("get.loadbalancer.for.freeswitch.per.context.ip." + context.name(), getFreeswitchIP());
    }

    public String getFreeswitchIP(FreeswitchContext context) {
        return getFreeswitchIP();
    }

    public String getVoiceForCompany(String company) {
        return config.getString("company.ivr.voice." + company, "ava");
    }

    public Boolean enableAnswerOnInboundSBCIncomingDialerOrder() {
        return config.getBoolean("enable.answer.on.inbound.SBC.incoming.dialer.order", true);
    }

    public String getCompanyInfo() {
        String value = config.getString("company.info.for.tms.wav", "CASHCALL");

        if (isAppx() == false) {
            String jvmParam = System.getProperty("company.id");
            if (jvmParam != null) {
                value = jvmParam.toUpperCase();
            }
        }
        log.info("Using company value info as {}", value);
        ivrVoice = getVoiceForCompany(value);

        return value;
    }

    public boolean sendCDRSyncToSVCAtStartOfDialplan() {
        return config.getBoolean("send.cdr.sync.to.svc.at.start.of.dialplan", true);
    }

    public boolean getcallTimeoutForDialerCallDirection(CallDirection callDirection) {
        return config.getBoolean("call.timeout.on.dialer.call.direction." + callDirection.name(), true);
    }

    public long getcallTimeoutForDialerInMillSec() {
        return config.getLong("call.timeout.on.dialer.is.mil.sec", 3600000l);
    }

    public String getDefaultVoicmailEmail() {
        return config.getString("default.voicemail.email.all", "hussien.sleiman@objectbrains.com");
    }

    public int getStartWorkingHourGlobal() {
        return config.getInteger("global.start.working.hour.of.day", 8);
    }

    public int getStartWorkingHourGlobal(int dayOfWeek) {
        return config.getInteger("global.start.working.hour.of.day." + dayOfWeek, getStartWorkingHourGlobal());
    }

    public int getStartWorkingHourGlobal(int dayOfWeek, String destinationNumber) {
        return config.getInteger("global.start.working.hour.of.day." + destinationNumber + "." + dayOfWeek, getStartWorkingHourGlobal(dayOfWeek));
    }

    public int getStartWorkingMinuteOfHourGlobal() {
        return config.getInteger("global.start.working.minute.of.hour", 0);
    }

    public int getStartWorkingMinuteOfHourGlobal(int dayOfWeek) {
        return config.getInteger("global.start.working.minute.of.hour." + dayOfWeek, getStartWorkingMinuteOfHourGlobal());
    }

    public int getStartWorkingMinuteOfHourGlobal(int dayOfWeek, String destinationNumber) {
        return config.getInteger("global.start.working.minute.of.hour." + destinationNumber + "." + dayOfWeek, getStartWorkingMinuteOfHourGlobal(dayOfWeek));
    }

    public int getEndWorkingHourGlobal() {
        return config.getInteger("global.end.working.hour.of.day", 20);
    }

    public int getEndWorkingHourGlobal(int dayOfWeek) {
        return config.getInteger("global.end.working.hour.of.day." + dayOfWeek, getEndWorkingHourGlobal());
    }

    public int getEndWorkingHourGlobal(int dayOfWeek, String destinationNumber) {
        return config.getInteger("global.end.working.hour.of.day." + destinationNumber + "." + dayOfWeek, getEndWorkingHourGlobal(dayOfWeek));
    }

    public int getEndWorkingMinuteOfHourGlobal() {
        return config.getInteger("global.end.working.minute.of.hour", 0);
    }

    public int getEndWorkingMinuteOfHourGlobal(int dayOfWeek) {
        return config.getInteger("global.end.working.minute.of.hour." + dayOfWeek, getEndWorkingMinuteOfHourGlobal());
    }

    public int getEndWorkingMinuteOfHourGlobal(int dayOfWeek, String destinationNumber) {
        return config.getInteger("global.end.working.minute.of.hour." + destinationNumber + "." + dayOfWeek, getEndWorkingMinuteOfHourGlobal(dayOfWeek));
    }

    public long getPutCallOnWaitAsyncDelaySetting1() {
        return config.getLong("put.call.on.wait.async.Delay.setting.1", 2000l);
    }

    public long getPutCallOnWaitAsyncDelaySetting2() {
        return config.getLong("put.call.on.wait.async.Delay.setting.2", 5000l);
    }

    public Boolean getUploadRecordingOnTouch() {
        return config.getBoolean("upload.recoding.on.touch", Boolean.FALSE);
    }

    public Integer getUploadRecordingQueueMax() {
        return config.getInteger("upload.recoding.queue.max", 20);
    }

    public Boolean getUploadRecordingChangeToDefaultIP() {
        return config.getBoolean("upload.recoding.change.to.default.ip", false);
    }

    public String getUploadRecordingDefaultIP() {
        return config.getString("upload.recoding.default.ip", getFreeswitchIP());
    }

    public String getUploadRecordingURL() {
        return config.getString("upload.recoding.url", "http://localhost:7070/recording-upload/upload");
    }

    public Boolean getUploadRecordingEndOfCall() {
        return config.getBoolean("upload.recoding.end.of.call", Boolean.FALSE);
    }

    public Long getCallDurationForSpeechToTextLimit() {
        return config.getLong("call.duration.for.speech.to.text.limit", 45000l);
    }

    public Boolean enableSpeechToTextTranslation() {
        return config.getBoolean("enable.speech.to.text.translations", true);
    }

    public Boolean enableSpeechToTextTranslationWhileUploadingRecoding() {
        return config.getBoolean("enable.speech.to.text.translation.while.uploading.recording", true);
    }

    public String getSpeechToTextTranslationOffloadingServerIP() {
        return config.getString("get.speech.to.text.translation.offloading.server.ip", "127.0.0.1");
    }

    public String getDefaultCallerIdNumber() {
        return config.getString("freeswitch.default.caller.id.number", "8559732886");
    }

    public String getOutboundBeepOnDuration() {
        String value = config.getString("freeswitch.outbound.beep.on.duration", "100");
        if (value.equals("NA")) {
            return "200";
        }
        return value;
    }

    public String getOutboundBeepOffDuration() {
        String value = config.getString("freeswitch.outbound.beep.off.duration", "25");
        if (value.equals("NA")) {
            return "100";
        }
        return value;
    }

    public String getOutboundBeepOfHZ() {
        String value = config.getString("freeswitch.outbound.beep.hz", "800");
        if (value.equals("NA")) {
            return "100";
        }
        return value;
    }

    public String getOutboundBeepVolume() {
        String value = config.getString("freeswitch.outbound.beep.volume", "v=-30;");
        if (value.equals("NA")) {
            return null;
        }
        return value;
    }

    public Boolean getOutboundBeepUseNew() {
        return config.getBoolean("freeswitch.outbound.beep.use.new", false);
    }

    public String getOutboundBeepLapseSpace() {
        String value = config.getString("freeswitch.outbound.beep.lapse.space", "15");
        if (value.equals("NA")) {
            return "15";
        }
        return value;
    }

    public Integer getGatewayVersion() {
        return config.getInteger("freeswitch.gateway.version.id", 0);
    }

    public Integer getIVRVersion() {
        return config.getInteger("freeswitch.ivr.version.id", 2);
    }

    public boolean enableDirectRoutingForInboundDistinationNumber() {
        return config.getBoolean("enable.direct.routing.for.inbound.distination.number", true);
    }

    public String getPowerPlayBeepToAgentHZ() {
        return config.getString("power.dialer.play.beep.to.agent.hz", "1000");
    }

    public String getPowerPlayBeepToAgentDuration() {
        return config.getString("power.dialer.play.beep.to.agent.duration", "200");
    }

    public boolean getDetectBusyToneOnAMD() {
        return config.getBoolean("amd.detect.tone.busy", false);
    }

    public boolean getDetectSITToneOnAMD() {
        return config.getBoolean("amd.detect.tone.sit", false);
    }

    public boolean getDetect3BusyToneOnAMD() {
        return config.getBoolean("amd.detect.tone.3.busy", false);
    }

    public Long getDetect3BusyToneTimeoutOnAMD() {
        return config.getLong("amd.detect.tone.3.busy.timeout", 15000l);
    }

    public boolean getExecuteOnDetectTone() {
        return config.getBoolean("amd.execute.on.detect.tone", false);
    }

    public String getAMDStartPlayBeep() {
        return config.getString("amd.start.play.beep", "L=2;%(100, 3000, 800);loops=1 mux");
    }

    public long getAMDSleepTime() {
        return config.getLong("amd.sleep.time", 20000l);
    }

    public boolean isAppx() {
        return config.getBoolean("is.appx", false) && config.getBoolean("is.in.test.mode", false);
    }

    public String getAMDPlayBeepToAgentDuration() {
        return config.getString("amd.play.beep.to.agent.duration", "600");
    }

    public String getMaxCallOnHoldAllowed() {
        return config.getString("max.call.length.allowed.to.be.on.hold.sec", "300");
    }

    public String getMaxHoldAnnounceTimeInSec() {
        return config.getString("max.fifo.hold.announce.time", "60");
    }

    public String getVoicemailLengthAllowed() {
        return config.getString("voicemail.length.allowed.global.sec", "120");
    }

    public String getInboundPlayBeepToAgentDuration() {
        return config.getString("inbound.play.beep.to.agent.duration", "600");
    }

    public String getAMDPlayBeepToAgentHZ() {
        return config.getString("amd.play.beep.to.agent.hz", "1692");
    }

    public String getInboundPlayBeepToAgentHZ() {
        return config.getString("inbound.play.beep.to.agent.hz", "892");
    }

    public String getAMDPlayBeepToAgentLast() {
        return config.getString("amd.play.beep.to.agent.last", "1 mux w");
    }

    public String getInboundPlayBeepToAgentLast() {
        return config.getString("inbound.play.beep.to.agent.last", "1 mux w");
    }

    public String getAMDWaitForSilence() {
        return config.getString("amd.wait.for.silence", "300 30 5 5000");
    }

    public long getAMDWaitForSilenceSleep() {
        return config.getLong("amd.wait.for.silence.sleep", 1000l);
    }

    public int getAMDWaitForSilenceCount() {
        return config.getInteger("amd.wait.for.silence.count", 3);
    }

    public int getACHConfigurationDayCutoffForIVR() {
        return config.getInteger("ach.configuration.day.cutoff.for.ivr", 15);
    }

    public boolean enableRecodingOnAgentToAgentDialplan() {
        return config.getBoolean("enable.recoding.on.agent.to.agent.diaplan", true);
    }

    public boolean useSipTable() {
        return config.getBoolean("use.sip.table.for.freeswitch", false);
    }

    private static String ivrVoice = "samantha";

    public static String getIVRVoice() {

        return ivrVoice;
        //return "samantha";
        //return "susan";
        //return "alex";
    }

    public static String getIVRRate() {
        return "8000";
    }

    public static String formatToYYYY_MM_DD(LocalDateTime d) {
        String month = d.getMonthOfYear() + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = d.getDayOfMonth() + "";
        if (day.length() == 1) {
            day = "0" + day;
        }
        return d.getYear() + "_" + month + "_" + day;
    }

    public static String getPhoneRecordingBucket() {
        return GCESignedUtility.GetGoogleProjectId() + "-phone-recording";
    }

    public String getFiFoHoldMusic() {
        return getHoldMusic();
    }

    public static String getHoldMusic() {
        String[] holds = {"DiscoHold_converted.wav", "MatrixHold_converted.wav", "RedneckHold_converted.wav", "SadHold_converted.wav", "fifo1.wav", "fifo2.wav", "fifo1.wav", "fifo2.wav", "fifo1.wav", "fifo2.wav", "fifo3.wav", "fifo3.wav"};
        Random ran = new Random();
        int x = ran.nextInt(holds.length);
        return SOUND_PATH + getIVRRate() + "/" + holds[x];
    }

    public String getFiFoForDialer() {
        return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/custom/" + config.getString("amd.fifo.hold.music", "Outbound_Queued_Hold_Message") + ".wav";
    }

    public String getFiFoForDialer(String name) {
        return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/custom/" + config.getString("amd.fifo.hold.music", name) + ".wav";
    }

    public String getRecordingFile(String name) {
        return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/custom/" + name + ".wav";
    }

    public String getRecordingFile(Integer integer) {
        if (integer < 1000) {
            return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/0_numbers/" + integer + ".wav";
        } else {
            return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/1000_numbers/" + integer + ".wav";
        }
    }

    public String getRecordingForAmountFile(Double integer) {
        return getRecordingForAmountFileStatic(integer);
    }
    
    public static String getRecordingForAmountFileStatic(Double integer) {
        return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/amounts/" + String.format("%1.2f", integer) + ".wav";
    }

    public static String getRecordingFileStatic(Integer integer) {
        if (integer < 1000) {
            return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/0_numbers/" + integer + ".wav";
        } else {
            return IVR_PATH + getIVRVoice() + "/" + getIVRRate() + "/1000_numbers/" + integer + ".wav";
        }
    }

}
