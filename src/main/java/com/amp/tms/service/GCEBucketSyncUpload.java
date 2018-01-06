/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.service;

import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import static com.amp.tms.constants.Constants.SCREENSHOTS_PATH;
import static com.amp.tms.constants.Constants.SCRIPTS_PATH;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author hsleiman
 */
@Service
public class GCEBucketSyncUpload {

    final ConcurrentHashMap<String, String> systemInfo = new ConcurrentHashMap<>();

    @ConfigContext
    private ConfigurationUtility configuration;

    private static final Logger LOG = LoggerFactory.getLogger(GCEBucketSyncUpload.class);

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    protected void executeInternal() throws JobExecutionException {
        if (configuration.getBoolean("screenrecoding.upload.backup.clean.enabled", false)) {
            LOG.info("Syncing Local Files with GCE Bucket...");
            try {
                executeScreenShotsSync();
            } catch (IOException ex) {
                LOG.error("Error Could not Sync Screenshots to GCE Bucket: " + ex.getMessage());
            } catch (InterruptedException ex) {
                LOG.error("Error Could not Sync Screenshots to GCE Bucket: " + ex.getMessage());
            }
        }
    }

    public void executeScreenShotsSync() throws IOException, InterruptedException {
        if (configuration.getBoolean("screenrecoding.upload.backup.clean.enabled", false)) {
            verifyFiles(FreeswitchConfiguration.formatToYYYY_MM_DD(LocalDateTime.now()));
        }
    }

    public void verifyFiles(String source) throws IOException, InterruptedException {
        String localSource = SCREENSHOTS_PATH + source;
        LOG.debug("Validate Local Source Files: " + localSource);
        String bucketSource = "gs://" + FreeswitchConfiguration.getPhoneRecordingBucket() + "/" + source;
        LOG.debug("Validate Bucket Source Files: " + bucketSource);
        Long cutOff = (System.currentTimeMillis() - (1000 * 120));
        LOG.debug("Validate Cut Off: " + cutOff + " Current: " + System.currentTimeMillis() + " elapse: " + (System.currentTimeMillis() - cutOff));

        File rhash = new File(SCRIPTS_PATH + "rhash.sh");
        if (rhash.exists() == false) {
            FileUtils.writeStringToFile(rhash, (IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/tms/rhash"))));
            executeCommand("chmod", "650 " + rhash.getAbsolutePath());
            executeCommand("chmod", "+x " + rhash.getAbsolutePath());
            rhash.setExecutable(true);
        }

        File rstat = new File(SCRIPTS_PATH + "rstat.sh");
        if (rstat.exists() == false) {
            FileUtils.writeStringToFile(rstat, (IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/tms/rstat"))));
            executeCommand("chmod", "650 " + rstat.getAbsolutePath());
            executeCommand("chmod", "+x " + rstat.getAbsolutePath());
            rstat.setExecutable(true);
        }

        boolean sync = false;

        HashSet<String> localFiles = walkDirectory(new HashSet<String>(), localSource, cutOff);
        for (String localfile : localFiles) {
            String bucketFile = localfile.replaceAll(localSource, bucketSource);
            String localHash = getLocalFileHash(localfile, rhash);
            String bucketHash = getBucketFileHash(bucketFile, rstat);
            LOG.debug("Comparing\n" + localfile + "\n" + bucketFile + "\n" + "Local HASH:" + localHash + "\n" + "Bucket HASH:" + bucketHash);
            boolean same = localHash.equals(bucketHash);
            LOG.debug("File verfied: " + same);
            if (localHash.startsWith("File Not Found") && bucketHash.startsWith("File Not Found") == false) {
                LOG.info("File Already Verified and Uploaded (No Action Needed): " + localfile);
            } else if (localHash.startsWith("File Not Found") == false && bucketHash.startsWith("File Not Found")) {
                LOG.info("File Missing (Action Needed): " + localfile);
                sync = true;
            } else if (same == false) {
                LOG.info("File NOT Verified Need to be synced File (Action Needed): " + localfile);
                sync = true;
            } else {
                LOG.info("File Verified Deleting File: " + localfile);
                File file = new File(localfile);
                removeFileAndParentsIfEmpty(file.toPath());
            }
        }
        if (sync) {
            LOG.info("File rsyncing File: " + localSource + " to " + bucketSource);
            rsyncFile(localSource, bucketSource);
            LOG.info("File rsynced File: " + localSource + " to " + bucketSource);
        }
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

    private void rsyncFile(String source, String target) throws IOException, InterruptedException {
        File rsyncFile = new File(SCRIPTS_PATH + "rsyncFile.sh");
        if (rsyncFile.exists() == false) {
            FileUtils.writeStringToFile(rsyncFile, (IOUtils.toString(ResourceUtils.getURL("classpath:com/objectbrains/tms/rsyncFile"))));
            executeCommand("chmod", "650 " + rsyncFile.getAbsolutePath());
            executeCommand("chmod", "+x " + rsyncFile.getAbsolutePath());
            rsyncFile.setExecutable(true);
        }
        executeCommand(rsyncFile.getAbsolutePath(), source, target);
    }

    private String getLocalFileHash(String file, File scriptFile) throws IOException, InterruptedException {
        String sourceHashString = "Not SET";
        try {
            LOG.debug("Checking File " + file);
            sourceHashString = executeCommand(scriptFile.getAbsolutePath(), file);
            LOG.debug("Text--------------------");
            LOG.debug("Text file: " + file);
            LOG.debug("Text hash: " + sourceHashString);
            LOG.debug("Text--------------------");
            if (sourceHashString.contains("No URLs matched")) {
                LOG.info("File Missing: " + file);
                return "File Not Found " + System.currentTimeMillis();
            }
            if (sourceHashString.contains("No files matched")) {
                LOG.info("File Missing: " + file);
                return "File Not Found " + System.currentTimeMillis();
            }
            String CRC32 = sourceHashString.substring(sourceHashString.indexOf("Hash (crc32c):") + "Hash (crc32c):".length()).trim();
            CRC32 = CRC32.substring(0, CRC32.indexOf("Hash (md5):")).trim();
            String MD5 = sourceHashString.substring(sourceHashString.indexOf("Hash (md5):") + "Hash (md5):".length()).trim();
            return CRC32 + MD5;
        } catch (IOException | StringIndexOutOfBoundsException | InterruptedException ex) {
            LOG.error("SOURCE TEST:\n\n" + sourceHashString);
            LOG.error("\n\nSOURCE TEST:");
            ex.printStackTrace();
            return "Something went wrong";

        }

    }

    private String getBucketFileHash(String file, File scriptFile) throws IOException, InterruptedException {
        String sourceHashString = "Not SET";
        try {
            LOG.debug("Checking File " + file);
            sourceHashString = executeCommand(scriptFile.getAbsolutePath(), file);
            LOG.debug("Text--------------------");
            LOG.debug("Text file: " + file);
            LOG.debug("Text Stat: " + sourceHashString);
            LOG.debug("Text--------------------");
            if (sourceHashString.contains("No URLs matched")) {
                LOG.info("Bucket Missing: " + file);
                return "File Not Found " + System.currentTimeMillis();
            }
            if (sourceHashString.contains("No files matched")) {
                LOG.info("Bucket Missing: " + file);
                return "File Not Found " + System.currentTimeMillis();
            }

            String CRC32 = sourceHashString.substring(sourceHashString.indexOf("Hash (crc32c):") + "Hash (crc32c):".length()).trim();
            CRC32 = CRC32.substring(0, CRC32.indexOf("Hash (md5):")).trim();
            String MD5 = sourceHashString.substring(sourceHashString.indexOf("Hash (md5):") + "Hash (md5):".length()).trim();
            MD5 = MD5.substring(0, MD5.indexOf("ETag:")).trim();
            return CRC32 + MD5;

        } catch (IOException | StringIndexOutOfBoundsException | InterruptedException ex) {
            LOG.error("SOURCE TEST:\n\n" + sourceHashString);
            LOG.error("\n\nSOURCE TEST:");
            ex.printStackTrace();
            return "Something went wrong";

        }
    }

    private HashSet<String> walkDirectory(HashSet<String> files, String path, long cutOff) {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return files;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                //LOG.info("Directory: " + f.getAbsoluteFile());
                files.addAll(walkDirectory(files, f.getAbsolutePath(), cutOff));
            } else {
                //LOG.info("File:" + f.getAbsoluteFile());
                BasicFileAttributes attr;
                try {
                    attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                    if (attr.creationTime().toMillis() < cutOff) {
                        files.add(f.getAbsolutePath());
                    }
                } catch (IOException ex) {
                    LOG.error("Adding in case File Issue: " + ex.getMessage());
                    files.add(f.getAbsolutePath());
                }

            }
        }
        return files;
    }

    private void removeFileAndParentsIfEmpty(Path path) throws IOException {

        if (path == null || path.startsWith(SCREENSHOTS_PATH) == false || path.equals(SCREENSHOTS_PATH)) {
            LOG.warn("THE PATH OF DELETION DOES NOT MATCH: " + path.toString() + " ==>> " + SCREENSHOTS_PATH);
            return;
        }
        if (Files.isRegularFile(path)) {
            Files.deleteIfExists(path);
        } else if (Files.isDirectory(path)) {
            try {
                Files.delete(path);
            } catch (DirectoryNotEmptyException e) {
                return;
            }
        }

        if ((path.getParent().toString() + "/").equalsIgnoreCase(SCREENSHOTS_PATH)) {
            return;
        }

        removeFileAndParentsIfEmpty(path.getParent());
    }

}
