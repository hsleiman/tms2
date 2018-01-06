
package com.amp.crm.embeddable;

import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlType;
import org.joda.time.Duration;

public class BIPlaybackData {

    public BIPlaybackData() {
        callLength = Duration.ZERO;
        playbackElements = Collections.emptyList();
    }

    private Duration callLength;

    private List<PlaybackElement> playbackElements;
    
    private String audioUrl;

    public Duration getCallLength() {
        return callLength;
    }

    public void setCallLength(Duration callLength) {
        this.callLength = callLength;
    }

    public List<PlaybackElement> getPlaybackElements() {
        return playbackElements;
    }

    public void setPlaybackElements(List<PlaybackElement> playbackElements) {
        this.playbackElements = playbackElements;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    @XmlType(name = "")
    public static class PlaybackElement {
        
        private int ext;
        private Duration callTime;
        private String imgUrl;

        public int getExt() {
            return ext;
        }

        public void setExt(int ext) {
            this.ext = ext;
        }

        public Duration getCallTime() {
            return callTime;
        }

        public void setCallTime(Duration callTime) {
            this.callTime = callTime;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

    }
}
