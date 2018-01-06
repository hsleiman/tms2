/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.pojo;

import java.util.Collections;
import java.util.List;
import org.joda.time.Duration;

/**
 *
 * @author connorpetty
 */
public class BiPlaybackData {

    public BiPlaybackData() {
        callLength = Duration.ZERO;
        playbackElements = Collections.emptyList();
    }

    private Duration callLength;

    private List<PlaybackElement> playbackElements;

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
