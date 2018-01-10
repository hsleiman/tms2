package com.amp.crm.db.configurations;

import com.amp.crm.pojo.UserData;
import java.util.HashMap;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class ThreadAttributes implements DisposableBean{

    private static ThreadLocal<HashMap<String, Object>> threadObjectAttrs = new ThreadLocal<HashMap<String, Object>>() {
        @Override
        protected HashMap<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private static ThreadLocal<HashMap<String, String>> threadStringAttrs = new ThreadLocal<HashMap<String, String>>() {
        @Override
        protected HashMap<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    private static ThreadLocal<Boolean> manualCommitInProgress = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private static HashMap<String, String> getStringHM() {
        HashMap<String, String> hm = threadStringAttrs.get();
        return hm;
    }

    public static String getString(String key) {
        return getStringHM().get(key);
    }

    public static void setString(String key, String value) {
        getStringHM().put(key, value);
    }

    private static HashMap<String, Object> getObjectHM() {
        HashMap<String, Object> hm = threadObjectAttrs.get();
        return hm;
    }

    public static Object get(String key) {
        if (key != null && key.equalsIgnoreCase("agent.username")) {
            Object obj = getObjectHM().get(key);
            return getUserData(obj);
        }
         return getObjectHM().get(key);
    }

    public static void set(String key, Object value) {
        getObjectHM().put(key, value);
    }

    public static void setManualCommitInProgress(boolean mcip) {
        manualCommitInProgress.set(mcip);
    }

    public static boolean isManualCommitInProgress() {
        return manualCommitInProgress.get();
    }

    public static void clear(boolean forceClear) {
        if (!forceClear && isManualCommitInProgress()) {
            return;
        }
        getStringHM().clear();
        getObjectHM().clear();
    }

    public static UserData getUserData(Object obj) {
        UserData userData = new UserData();
        if (obj == null) {
            userData.setUserName("OriginationSystem");
            userData.setBypassSecurity(Boolean.TRUE);
        } else {
            try {
                userData = (UserData)obj;
                if (userData.getUserName() == null || userData.getUserName().trim().equals("")) {
                    userData.setUserName("OriginationSystem");
                    userData.setBypassSecurity(Boolean.TRUE);
                }
            } catch (Exception e) {
                userData.setUserName("OriginationSystem");
                userData.setBypassSecurity(Boolean.TRUE);
            }
        }
        return userData;
    }

    @Override
    public void destroy() {
        threadObjectAttrs = null;
        threadStringAttrs = null;
        manualCommitInProgress = null;
    }
    
    public static String getTranstionAndThreadInfo(){
        return getString("current.session.log.info");
    }
    
}
