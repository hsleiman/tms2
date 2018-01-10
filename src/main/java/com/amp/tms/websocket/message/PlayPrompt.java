/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.websocket.message;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.amp.tms.enumerated.PaymentType;
import com.amp.tms.enumerated.PromptType;
import com.amp.tms.enumerated.RecordedPhrases;
import com.amp.tms.enumerated.RecordedWords;
import com.amp.tms.freeswitch.pojo.FreeswitchCommand;
import com.amp.tms.service.FreeswitchConfiguration;
import com.amp.tms.service.TextToSpeechService;
import com.amp.tms.utility.GsonUtility;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 
 */
public class PlayPrompt implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(PlayPrompt.class);

    @Expose
    private PromptType promptType;

    @Expose
    private PaymentType paymentType;

    @Expose
    private Integer subType;

    @Expose
    private Double amount;

    @Expose
    private Double totalAmount;

    @Expose
    private LocalDate todaysDate;

    @Expose
    private LocalDate date;

    @Expose
    private String accNumber;

    @Expose
    private String accRouting;

    @Expose
    private Double convenienceFee;

    @Expose
    private String freeswitchIp;

    @Expose
    private Integer extOnCall;

    @Expose
    private String freeswitchChannalId;

    TextToSpeechService textToSpeechService = new TextToSpeechService();

    public PlayPrompt() {
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public PromptType getPromptType() {
        return promptType;
    }

    public void setPromptType(PromptType promptType) {
        this.promptType = promptType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public void setAccNumber(String accNumber) {
        this.accNumber = accNumber;
    }

    public String getAccRouting() {
        return accRouting;
    }

    public void setAccRouting(String accRouting) {
        this.accRouting = accRouting;
    }

    public Double getConvenienceFee() {
        return convenienceFee;
    }

    public void setConvenienceFee(Double convenienceFee) {
        this.convenienceFee = convenienceFee;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getExtOnCall() {
        return extOnCall;
    }

    public void setExtOnCall(Integer extOnCall) {
        this.extOnCall = extOnCall;
    }

    public String getFreeswitchChannalId() {
        return freeswitchChannalId;
    }

    public void setFreeswitchChannalId(String freeswitchChannalId) {
        this.freeswitchChannalId = freeswitchChannalId;
    }

    public String getFreeswitchIp() {
        return freeswitchIp;
    }

    public void setFreeswitchIp(String freeswitchIp) {
        this.freeswitchIp = freeswitchIp;
    }

    public LocalDate getTodaysDate() {
        return todaysDate;
    }

    public void setTodaysDate(LocalDate todaysDate) {
        this.todaysDate = todaysDate;
    }

    public ArrayList<FreeswitchCommand> buildWithBroadCast() {
        if (textToSpeechService == null) {
            textToSpeechService = new TextToSpeechService();
        }

        log.info("Starting Broad cast build...");

        try {
            ArrayList<FreeswitchCommand> list = new ArrayList<>();
            log.info("promptType {}", promptType);
            switch (promptType) {
                case PAYMENT:
                    log.info("paymentType {}", paymentType);
                    switch (paymentType) {
                        case CREDIT_CARD:
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.THE_TOTAL_PAYMENT_OF.getAudioPath(), "both"));

                            playbackAmount(list, getTotalAmount());

                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.WILL_BE_CHARGED_ON.getAudioPath(), "both"));

                            LocalDate dateValue = getDate();
                            addDate(list, dateValue);

                            if (getConvenienceFee() > 0) {
                                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.THIS_CHARGE_INCLUDES_THE.getAudioPath(), "both"));

                                playbackAmount(list, getAmount());
                                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.PAYMENT_AND_A.getAudioPath(), "both"));

                                playbackAmount(list, getConvenienceFee());
                                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.CONVENIENCE_FEE.getAudioPath(), "both"));
                            }
                            if (getSubType() == 2) {
                                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.IF_PAYMENT_IS_UNSUCCESSFULL_REATTEMPT_12PM.getAudioPath(), "both"));
                                addDate(list, dateValue);
                            }

                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.WOULD_YOU_LIKE_TO_PROCEED.getAudioPath(), "both"));
                            break;
                        case ACH:
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedWords.TODAY.getAudioPath(), "both"));
                            dateValue = getTodaysDate();
                            addDate(list, dateValue);
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.AUTHORIZING_CASHCALL_ACH_AMOUNT.getAudioPath(), "both"));

                            playbackAmount(list, getTotalAmount());
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.FROM_ACCOUNT.getAudioPath(), "both"));
                            String value = getAccNumber();
                            for (int i = 0; i < value.length(); i++) {
                                String get = value.charAt(i) + "";
                                try {
                                    Integer.parseInt(get);
                                    addNumbers(list, get);
                                } catch (NumberFormatException ex) {
                                    log.error("Number count not be parsed {} - {}", get, ex);
                                }

                            }
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.WITH_ROUTING.getAudioPath(), "both"));
                            value = getAccRouting();
                            for (int i = 0; i < value.length(); i++) {
                                String get = value.charAt(i) + "";
                                try {
                                    Integer.parseInt(get);
                                    addNumbers(list, get);
                                } catch (NumberFormatException ex) {
                                    log.error("Number count not be parsed {} - {}", get, ex);
                                }
                            }

                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.ON_OR_AFTER.getAudioPath(), "both"));
                            if (getSubType() == 2) {
                                dateValue = getDate();
                            }
                            addDate(list, dateValue);
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.IF_CREATE_CHECK_NAME_ON_SIGNATURE.getAudioPath(), "both"));
                            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.WOULD_YOU_LIKE_TO_PROCEED.getAudioPath(), "both"));
                            break;
                        default:
                            break;
                    }
                    break;
                case CUSTOMER_VERBAL:
                    list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedPhrases.CUSTOMER_VERBAL_AGREEMENT.getAudioPath(), "both"));
                    break;
            }

            for (int i = 0; i < list.size(); i++) {
                FreeswitchCommand get = list.get(i);
                log.info("Executing Prompt with Command {} arg {} sleep {}", get.getCommand(), get.getArg(), get.getSleeptime());

            }

            return list;
        } catch (Exception ex) {
            log.error("Exception {}", ex);
            return new ArrayList<>();
        }

    }

    private void playbackAmount(ArrayList<FreeswitchCommand> list, Double amount) throws Exception {
        if (amount < 1000.00) {
            list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), FreeswitchConfiguration.getRecordingForAmountFileStatic(amount), "both"));
        } else {
            addAmount(list, amount);
        }
    }

    private void addAmount(ArrayList<FreeswitchCommand> list, Double amount) throws Exception {
        String value = amount + "";
        List<Integer> digits = textToSpeechService.getNumberPattern(value.substring(0, value.indexOf(".")));
        if (digits != null && digits.isEmpty() == false) {
            for (Integer get : digits) {
                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), FreeswitchConfiguration.getRecordingFileStatic(get), "both"));
            }
        } else {
            throw new Exception("Invalid propmpt");
        }
        list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedWords.DOLLARS.getAudioPath(), "both"));
        if ((value.substring(value.indexOf(".")).equalsIgnoreCase("00") == false)|| (value.substring(value.indexOf(".")).equalsIgnoreCase("0") == false)) {
            digits = textToSpeechService.getNumberPattern(value.substring(value.indexOf(".") + 1));
            if (digits != null && digits.isEmpty() == false) {
                for (Integer get : digits) {
                    list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), FreeswitchConfiguration.getRecordingFileStatic(get), "both"));
                }
                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedWords.CENTS.getAudioPath(), "both"));
            } else {
                throw new Exception("Invalid propmpt");
            }
        }
    }

    private void addDate(ArrayList<FreeswitchCommand> list, LocalDate value) {
        list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedWords.getMonthAndDay(value).getAudioPath(), "both"));
        list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), RecordedWords.getYear(value.getYear()).getAudioPath(), "both"));
    }

    private void addNumbers(ArrayList<FreeswitchCommand> list, String value) throws Exception {
        List<Integer> digits = textToSpeechService.getNumberPattern(value);
        if (digits != null && digits.isEmpty() == false) {
            for (Integer get : digits) {
                list.add(new FreeswitchCommand("uuid_broadcast", getFreeswitchIp(), getFreeswitchChannalId(), FreeswitchConfiguration.getRecordingFileStatic(get), "both"));
            }
        } else {
            throw new Exception("Invalid propmpt");
        }
    }
    
    public String toJson() {
        Gson gson = GsonUtility.getGson(true);
        return gson.toJson(this);
    }
}
