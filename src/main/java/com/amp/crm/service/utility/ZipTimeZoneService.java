/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.utility;

import com.objectbrains.fileutility.file.compression.zipFile;
import com.objectbrains.fileutility.http.HttpDownloadFile;
import com.objectbrains.hcms.annotation.ConfigContext;
import com.objectbrains.hcms.configuration.ConfigurationUtility;
import com.amp.crm.constants.DoNotCallCodes;
import com.amp.crm.db.entity.utility.ZipTimezone;
import com.amp.crm.db.repository.utility.ZipTimeZoneRepository;
import com.amp.crm.exception.StiException;
import com.amp.crm.pojo.CustomerCallablePojo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author David
 */
@Service
@Transactional
public class ZipTimeZoneService {
    @Autowired
    private ZipTimeZoneRepository zipRepo;
    @ConfigContext
    private ConfigurationUtility config;

    public static final Logger LOG = LoggerFactory.getLogger(ZipTimeZoneService.class);

    private static String[] tollFreeAreaCodes = {"800", "844", "855", "866", "877", "888"};

    public List<ZipTimezone> getLocationInfoByZip(String zip) throws StiException {

        if (StringUtils.isNotBlank(zip)) {
            List<ZipTimezone> locInfo = zipRepo.getLocationInfoByZip(zip);
            return locInfo;

        } else {
            throw new StiException("Zip Code entered cannot be null or empty");
        }
    }

    public List<ZipTimezone> getLocationInfoByAreaCode(Integer areaCode) throws StiException {
        if (areaCode != null) {
            List<ZipTimezone> locInfo = zipRepo.getLocationInfoByAreaCode(areaCode);
            return locInfo;

        } else {
            throw new StiException("AreaCode entered cannot be null");
        }
    }

    public CustomerCallablePojo getCustomerCallable(String zipAddress, Long areaCode) {
        CustomerCallablePojo pojo = new CustomerCallablePojo();
        List<ZipTimezone> zipList = new ArrayList<>();
        Set<ZipTimezone> matchedTimeZoneSet = new HashSet<>();
        if(phoneIsTollFreeAreaCode(String.valueOf(areaCode))){
            pojo.setDoNotCallCode(DoNotCallCodes.OKAY_TO_CALL);
            return pojo;
        }
        if (StringUtils.isNotBlank(zipAddress)) {
            matchedTimeZoneSet.addAll(zipRepo.getLocationInfoByZip(zipAddress));
        }
        if (areaCode != null && (areaCode > 0 && areaCode <= 999)) {
            matchedTimeZoneSet.addAll(zipRepo.getLocationInfoByAreaCode(areaCode.intValue()));
        }
        zipList.addAll(matchedTimeZoneSet);
        if (zipList.isEmpty()) {
            pojo.setDoNotCallCode(DoNotCallCodes.NO_DATABASE_MATCH);
            return pojo;
        }

        int tooEarlyToCallHour = config.getInteger("Borrower.TooEarlyToCall", 8);
        int tooLateToCallHour = config.getInteger("Borrower.TooLateToCall", 17);

        DateTimeZone timeZone = convertTimeZoneNow(zipList.get(0).getTimezone());
        DateTime now = DateTime.now(timeZone);
        DateTime early = now.withTime(tooEarlyToCallHour, 0, 0, 0);
        DateTime late = now.withTime(tooLateToCallHour, 0, 0, 0);
        pojo.setRightNow(now.toLocalDateTime());

        for (ZipTimezone zipTimezone : zipList) {//sets the conservative early and later call times if necessary
            timeZone = convertTimeZoneNow(zipTimezone.getTimezone());
            now = DateTime.now(timeZone);
            if (early.isBefore(now.withTime(tooEarlyToCallHour, 0, 0, 0))) {
                early = now.withTime(tooEarlyToCallHour, 0, 0, 0);
            }
            if (late.isAfter(now.withTime(tooLateToCallHour, 0, 0, 0))) {
                late = now.withTime(tooLateToCallHour, 0, 0, 0);
            }
        }
        now = DateTime.now();
        early = early.toDateTime(DateTimeZone.getDefault());
        late = late.toDateTime(DateTimeZone.getDefault());
        //earliest time to call borrower based on our timezone
        pojo.setTooEarly(early.toLocalDateTime());
        //latest time to call borrower based on our timezone
        pojo.setTooLate(late.toLocalDateTime());
        if (now.isBefore(early)) {
            pojo.setDoNotCallCode(DoNotCallCodes.TOO_EARLY_TO_CALL);
        } else if (now.isAfter(late)) {
            pojo.setDoNotCallCode(DoNotCallCodes.TOO_LATE_TO_CALL);
        } else {
            pojo.setDoNotCallCode(DoNotCallCodes.OKAY_TO_CALL);
        }
        return pojo;
    }

    public DateTimeZone convertTimeZoneNow(String zipTimeZoneString) {
        DateTimeZone timeZone = DateTimeZone.getDefault(); //should never have to use default. Need to add more cases as necessary
        switch (zipTimeZoneString) {
            case "Mountain":
                timeZone = DateTimeZone.forID("America/Denver");
                break;
            case "Central":
                timeZone = DateTimeZone.forID("America/Chicago");
                break;
            case "UTC+12":
                timeZone = DateTimeZone.forID("Etc/GMT+12");
                break;
            case "UTC+11":
                timeZone = DateTimeZone.forID("Etc/GMT+11");
                break;
            case "UTC+10":
                timeZone = DateTimeZone.forID("Etc/GMT+10");
                break;
            case "UTC+9":
                timeZone = DateTimeZone.forID("Etc/GMT+9");
                break;
            case "Eastern":
                timeZone = DateTimeZone.forID("America/New_York");
                break;
            case "Pacific":
                timeZone = DateTimeZone.forID("America/Los_Angeles");
                break;
            case "Hawaii":
                timeZone = DateTimeZone.forID("Pacific/Honolulu");
                break;
            case "Alaska":
                timeZone = DateTimeZone.forID("America/Anchorage");
                break;
            case "Atlantic":
                timeZone = DateTimeZone.forID("America/Puerto_Rico");
                break;
            default:
                System.out.println("No matching DateTimeZone found, using default DateTimeZone");
        }
        return timeZone;
    }

    public void zipCodeTimezoneTableSweep() throws StiException {
        String downloadLink = "https://zipcodedownload.com/Account/Download/?file=" + getMonth(LocalDate.now().minusMonths(1)) + LocalDate.now().minusMonths(1).getYear() + "_commercial_csv.zip&username=hussien.sleiman@objectbrains.com&password=Object13rain$User";
        String fileNameAndPath = SystemUtils.JAVA_IO_TMPDIR + "/" + getMonth(LocalDate.now().minusMonths(1)) + LocalDate.now().getYear() + "_commercial_csv.zip";
        File zippedFileName = null;
        File UnZippedDirectory = null;
        try {
            zippedFileName = HttpDownloadFile.Download(downloadLink, fileNameAndPath);
            UnZippedDirectory = zipFile.unZipFile(zippedFileName);
            File[] unZippedFiles = UnZippedDirectory.listFiles();
            File csvFile = null;
            for (File unZippedFile : unZippedFiles) {
                if (unZippedFile.getName().equals("5-digit Commercial.csv")) {
                    csvFile = unZippedFile;
                }
            }
            if (csvFile != null) {
                String line;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
                if ((line = bufferedReader.readLine()) == null) {
                    throw new StiException("empty file");
                }//clear the first line of CSV file which contains the column names
                zipRepo.truncate();
                //start threads
                while ((line = bufferedReader.readLine()) != null) {
                    parseCSV(line); // add lines to queue to be read by threads
                }
                bufferedReader.close();
            } else {
                throw new StiException("Unable to locate CSV File.");
            }

        } catch (IOException exc) {
            throw new StiException(exc.getMessage());
        } finally {
            cleanFiles(UnZippedDirectory, zippedFileName);
        }
    }

    private String getMonth(LocalDate now) throws StiException {
        String threeLetterMonth;
        int monthOfYear = now.getMonthOfYear();
        switch (monthOfYear) {
            case 1:
                threeLetterMonth = "jan";
                break;
            case 2:
                threeLetterMonth = "feb";
                break;
            case 3:
                threeLetterMonth = "mar";
                break;
            case 4:
                threeLetterMonth = "apr";
                break;
            case 5:
                threeLetterMonth = "may";
                break;
            case 6:
                threeLetterMonth = "jun";
                break;
            case 7:
                threeLetterMonth = "jul";
                break;
            case 8:
                threeLetterMonth = "aug";
                break;
            case 9:
                threeLetterMonth = "sep";
                break;
            case 10:
                threeLetterMonth = "oct";
                break;
            case 11:
                threeLetterMonth = "nov";
                break;
            case 12:
                threeLetterMonth = "dec";
                break;
            default:
                throw new StiException("Unable to create file name for zipcodedownload : Unknown month of year " + monthOfYear);
        }
        return threeLetterMonth;
    }

    private void parseCSV(String line) throws StiException {
        line = cleanExceptions(line);
        String[] zipTimezone = new String[16];
        ZipTimezone svZipTimezone;
        zipTimezone = line.split(",");
        String areaCode = zipTimezone[10].replaceAll(" ", "");
        int areaCodeDupe;
        boolean isZero = false;
        switch (areaCode.length()) {
            case 1:
                areaCodeDupe = 1;
                isZero = true;
                break;
            case 3:
                areaCodeDupe = 1;
                break;
            case 7:
                areaCodeDupe = 2;
                break;
            case 11:
                areaCodeDupe = 3;
                break;
            case 15:
                areaCodeDupe = 4;
                break;
            default:
                LOG.info("Areacode: {} has length {}", areaCode, areaCode.length());
                throw new StiException("area code length not within expected size");
        }
        for (int i = 0; i < areaCodeDupe; i++) {
            svZipTimezone = new ZipTimezone();
            String zip = zipTimezone[0];
            while (zip.length() < 5) {
                zip = "0" + zip;
            }
            svZipTimezone.setZip(zip);
            svZipTimezone.setZipCodeType(zipTimezone[1]);
            svZipTimezone.setCity(zipTimezone[2]);
            svZipTimezone.setCityType(zipTimezone[3]);
            svZipTimezone.setCountynm(zipTimezone[4]);
            svZipTimezone.setCounty_fips(Integer.parseInt(zipTimezone[5]));
            svZipTimezone.setStatename(zipTimezone[6]);
            svZipTimezone.setState(zipTimezone[7]);
            svZipTimezone.setState_fips(Integer.parseInt(zipTimezone[8]));
            svZipTimezone.setMsa(Integer.parseInt(zipTimezone[9].trim()));
            if (isZero) {
                svZipTimezone.setAreacode(0);
            } else if (i == 0) {
                svZipTimezone.setAreacode(Integer.parseInt(areaCode.substring(0, 3)));
            } else if (i == 1) {
                svZipTimezone.setAreacode(Integer.parseInt(areaCode.substring(4, 7)));
            } else if (i == 2) {
                svZipTimezone.setAreacode(Integer.parseInt(areaCode.substring(8, 11)));
            } else if (i == 3) {
                svZipTimezone.setAreacode(Integer.parseInt(areaCode.substring(12)));
            }
            svZipTimezone.setTimezone(zipTimezone[11]);
            svZipTimezone.setGmtoffset(new Double(zipTimezone[12]).intValue());
            svZipTimezone.setDst(zipTimezone[13]);
            svZipTimezone.setY_coord(Double.parseDouble(zipTimezone[14]));
            svZipTimezone.setX_coord(Double.parseDouble(zipTimezone[15]));
            zipRepo.saveZipTimezone(svZipTimezone);
        }
    }

    private void cleanFiles(File UnzippedDirectory, File csvFile) throws StiException {
        if (UnzippedDirectory != null) {
            if (UnzippedDirectory.exists()) {
                deleteDirectory(UnzippedDirectory);
            }
        }
        if (csvFile != null) {
            if (csvFile.exists()) {
                csvFile.delete();
            }
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        directory.delete();
    }

    private String cleanExceptions(String line) {
        String[] zipTimezone = new String[16];
        zipTimezone = line.split(",");
        if (zipTimezone[9] == "None" && zipTimezone[2] == "Kalaupapa") {
            zipTimezone[9] = "Hawaii";
            zipTimezone[12] = "-10";
        }
        String cleanedLine = "";
        for (String piece : zipTimezone) {
            cleanedLine = cleanedLine + piece + ",";
        }
        cleanedLine = cleanedLine.substring(0, cleanedLine.length() - 1);
        return cleanedLine;
    }
    
    private boolean phoneIsTollFreeAreaCode(String areaCode){
        List<String> tollFree = new ArrayList<>();
        tollFree.addAll(Arrays.asList( tollFreeAreaCodes));
        return tollFree.contains(areaCode);
    }
}
