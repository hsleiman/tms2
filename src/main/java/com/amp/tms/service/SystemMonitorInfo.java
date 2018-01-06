/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author hsleiman
 */
@Service
public class SystemMonitorInfo {

    @ConfigContext
    private ConfigurationUtility config;

    @Autowired
    private FreeswitchConfiguration configuration;



    private static final Logger LOG = LoggerFactory.getLogger(SystemMonitorInfo.class);

    final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> info = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> memoryInfo = new ConcurrentHashMap<>();

    final NumberFormat formatter = new DecimalFormat("#.##");

    @Scheduled(initialDelay = 6000, fixedDelay = 15000)
    public void SchedulerTimer() {
        try {
            if (config.getBoolean("enable.monitor", false)) {
                diskSpace();
                MemoryCheck();
                memInfo();
                vmstat();
            }
        } catch (IOException ex) {
            LOG.error("Error: " + ex.getMessage());
        } catch (InterruptedException ex) {
            LOG.error("Error: " + ex.getMessage());
        }

    }



   
    private void MemoryCheck() throws IOException, InterruptedException {
        String key = "free -m";
        String result = executeCommand("free", "-m");

        ConcurrentHashMap<String, String> map = memoryInfo.get(key);
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        String pastResult = map.get("all");
        if (pastResult == null) {
            LOG.info("[" + key + "] - Initial\n" + result);
        } else if (result.equalsIgnoreCase(pastResult) == false) {
            String[] arr = new String[3];
            arr[0] = pastResult;
            arr[1] = "   -->   ";
            arr[2] = result;
            LOG.info("[" + key + "] - Changed From\n" + printColumns(arr) + "\n");
        }
        map.put("all", result);
        memoryInfo.put(key, map);
    }

    private void diskSpace() throws IOException, InterruptedException {
        String key = "df -h";
        String result = executeCommand("df", "-h");

        ConcurrentHashMap<String, String> map = memoryInfo.get(key);
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        String pastResult = map.get("all");
        if (pastResult == null) {
            LOG.info("[" + key + "] - Initial\n" + result);
        } else if (result.equalsIgnoreCase(pastResult) == false) {
            String[] arr = new String[3];
            arr[0] = pastResult;
            arr[1] = "   -->   ";
            arr[2] = result;
            LOG.info("[" + key + "] - Changed From\n" + printColumns(arr) + "\n");
        }
        map.put("all", result);
        memoryInfo.put(key, map);

    }

    private void vmstat() throws IOException, InterruptedException {
        String key = "vmstat -s";
        String result = executeCommand("vmstat", "-s");
        ConcurrentHashMap<String, Long> pastResult = info.get(key);
        if (pastResult == null) {
            pastResult = new ConcurrentHashMap<>();
        }

        String[] lines = result.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String name = "";
            String value = "";
            String type = "";
            Long newValue = 0l;
            try {
                String line = lines[i];
                LOG.debug("Working on Line: " + line);
                line = line.trim();
                String[] tokens = line.split(" ");
                for (int j = 0; j < tokens.length; j++) {
                    String token = tokens[j];
                    if (j == 0) {
                        value = token;
                    } else if (j == 1 && token.equalsIgnoreCase("K")) {
                        type = token;
                    } else {
                        if (name.equals("")) {
                            name = token;
                        } else {
                            name = name + " " + token;
                        }
                    }
                }
                LOG.debug(name + " - " + value + " - " + type);
                Long oldValue = pastResult.get(name);
                newValue = Long.parseLong(value);
                if (isChanged(oldValue, newValue) && rateOfChange(oldValue, newValue) > 0.01) {
                    printSyetemInfo(key, name, oldValue, newValue, type, rateOfChange(oldValue, newValue));
                }
            } catch (Exception ex) {
                LOG.error("Error" + ex);
            }
            LOG.debug("Adding " + name + " - " + newValue);
            pastResult.put(name, newValue);
        }
        info.put(key, pastResult);
    }

    private void memInfo() throws IOException, InterruptedException {
        String key = "cat /proc/meminfo";
        String result = executeCommand("cat", "/proc/meminfo");
        ConcurrentHashMap<String, Long> pastResult = info.get(key);
        if (pastResult == null) {
            pastResult = new ConcurrentHashMap<>();
        }

        String[] lines = result.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String name = "";
            String value = "";
            Long newValue = 0l;
            try {
                String line = lines[i];
                LOG.debug("Working on Line: " + line);
                name = line.substring(0, line.indexOf(":")).trim();
                value = line.substring(line.indexOf(":") + 1).trim();
                if (value.contains(" ")) {
                    value = value.substring(0, value.indexOf(" "));
                }
                String type = "";
                if (value.lastIndexOf(" ") != -1) {
                    type = value.substring(value.lastIndexOf(" ")).trim();
                }
                Long oldValue = pastResult.get(name);
                newValue = Long.parseLong(value);
                if (isChanged(oldValue, newValue) && rateOfChange(oldValue, newValue) > 0.01) {
                    printSyetemInfo(key, name, oldValue, newValue, type, (rateOfChange(oldValue, newValue) * 100));
                }
            } catch (Exception ex) {
                LOG.error("Error" + ex);
            }
            LOG.debug("Adding " + name + " - " + newValue);
            pastResult.put(name, newValue);
        }
        info.put(key, pastResult);
    }

    private Double rateOfChange(Long oldValue, Long newValue) {
        if (oldValue == null || newValue == null) {
            return 0d;
        }
        if (Objects.equals(oldValue, newValue)) {
            return 0d;
        } else if (oldValue < newValue) {
            return 1 - (oldValue.doubleValue() / newValue.doubleValue());
        } else {
            return (1 - (newValue.doubleValue() / oldValue.doubleValue())) * -1;
        }
    }

    private boolean isChanged(Long oldValue, Long newValue) {
        if (Objects.equals(oldValue, newValue)) {
            return false;
        }
        return true;
    }

    private void printSyetemInfo(String key, String name, Long oldValue, Long newValue, String type, Double rateOfChange) throws IOException, InterruptedException {
        String number = formatter.format(rateOfChange);
        LOG.info("[" + key + "] - [" + name + "] change from [" + oldValue + "] to [" + newValue + "] " + type + " with rate of change of: " + number + "%");
    }

    private String executeCommand(String command, String arg) throws IOException, InterruptedException {
        LOG.debug("Echo command: " + command + " - " + arg);

        ProcessBuilder pb = new ProcessBuilder(command, arg);
        Process process = pb.start();
        int errCode = process.waitFor();

        String responseError = output(process.getErrorStream());
        LOG.debug("Echo Error Output:\n\n" + responseError + "\n");

        String responseOutput = output(process.getInputStream());
        LOG.debug("Echo Output:\n\n" + responseOutput + "\n");

        if (errCode == 0) {
            LOG.debug("XEcho command executed, any errors? NO");
            return responseOutput;
        } else {
            LOG.debug("XEcho command executed, any errors? YES");
            return responseError;
        }
    }

    private String executeCommand(String command, String arg1, String arg2) throws IOException, InterruptedException {
        LOG.debug("Echo command: " + command + " - " + arg1 + " - " + arg2);

        ProcessBuilder pb = new ProcessBuilder(command, arg1, arg2);
        Process process = pb.start();
        int errCode = process.waitFor();

        String responseError = output(process.getErrorStream());
        LOG.debug("Echo Error Output:\n\n" + responseError + "\n");

        String responseOutput = output(process.getInputStream());
        LOG.debug("Echo Output:\n\n" + responseOutput + "\n");

        if (errCode == 0) {
            LOG.debug("Echo command executed, any errors? NO");
            return responseOutput;
        } else {
            LOG.debug("Echo command executed, any errors? YES");
            return responseError;
        }
    }

    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }

    public String printColumns(String[] input) {
        String result = "";

        // Split input strings into columns and rows
        String[][] columns = new String[input.length][];
        int maxLines = 0;
        for (int i = 0; i < input.length; i++) {
            columns[i] = input[i].split("\n");
            if (columns[i].length > maxLines) {
                maxLines = columns[i].length;
            }
        }

        // Store an array of column widths
        int[] widths = new int[input.length];
        // calculate column widths
        for (int i = 0; i < input.length; i++) {
            int maxWidth = 0;
            for (int j = 0; j < columns[i].length; j++) {
                if (columns[i][j].length() > maxWidth) {
                    maxWidth = columns[i][j].length();
                }
            }
            widths[i] = maxWidth + 1;
        }

        // "Print" all lines
        for (int line = 0; line < maxLines; line++) {
            for (int column = 0; column < columns.length; column++) {
                String s = line < columns[column].length ? columns[column][line] : "";
                result += String.format("%-" + widths[column] + "s", s);
            }
            result += "\n";
        }
        return result;
    }
}
