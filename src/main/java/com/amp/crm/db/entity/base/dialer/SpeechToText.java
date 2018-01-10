/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.db.entity.base.dialer;

import com.amp.crm.db.entity.superentity.SuperEntity;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.Type;


@Entity
@Table(schema = "crm")
public class SpeechToText extends SuperEntity{
    
    private String callUUID;
    @Type(type="text")
    private String speechToText;
    @Type(type="text")
    private String leftText;
    @Type(type="text")
    private String rightText;
    @Type(type="text")
    private String keywordsInText;
    private String badLanguageList;
    private Long keywordPriority;
    private Long badLanguagePriority;
    private Boolean badLanguageInText;

    public SpeechToText(){
        
    }
    
    public SpeechToText(String callUUID){
        setCallUUID(callUUID);
    }
    public String getCallUUID() {
        return callUUID;
    }

    public void setCallUUID(String callUUID) {
        this.callUUID = callUUID;
    }

    public String getSpeechToText() {
        return speechToText;
    }

    public void setSpeechToText(String speechToText) {
        this.speechToText = speechToText;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }
    
    public String getKeywordsInText(){
        return keywordsInText;
    }
    
    public void setKeywords(String keywordsInText){
        this.keywordsInText = keywordsInText;
    }
    
    public String getBadLanguageList(){
        return badLanguageList;
    }
    
    public void setBadLanguageList(String badLanguageList){
        this.badLanguageList = badLanguageList;
    }
    
    public Long getKeywordPriority(){
        return keywordPriority;
    }
    
    public void setKeywordPriority(Long keywordPriority){
        this.keywordPriority = keywordPriority;
    }
    
    public Long getBadLanguagePriority(){
        return badLanguagePriority;
    }
    
    public void setBadLanguagePriority(Long badLanguagePriority){
        this.badLanguagePriority = badLanguagePriority;
    }
    
    public Boolean isBadLanguageInText(){
        return badLanguageInText;
    }
    
    public void setBadLanguageInText(Boolean badLanguageInText){
        this.badLanguageInText = badLanguageInText;
    }
}
