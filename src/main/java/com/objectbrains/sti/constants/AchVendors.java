/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.constants;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sundeeptaachanta
 */
public enum AchVendors {
    
    CARMEL(7, "CARMEL", "settleit", false,0);
    
    private final int vendorType;
    private final String vendorDesc;
    private final String companyId;
    private final boolean subCategory;
    private final int masterVendorId;
    
    private AchVendors(int vendorType, String vendorDesc, String companyId, boolean subCategory, int masterVendorId){
        this.vendorType=vendorType;
        this.vendorDesc=vendorDesc;
        this.companyId=companyId;
        this.subCategory=subCategory;
        this.masterVendorId = masterVendorId;
    }

    public String getCompanyId() {
        return companyId;
    }
    

    public int getVendorType() {
        return vendorType;
    }

    public String getVendorDesc() {
        return vendorDesc;
    }

    public boolean isSubCategory() {
        return subCategory;
    }

    public int getMasterVendorId() {
        return masterVendorId;
    }
    
    
    public static AchVendors getAchVendorById(int vendorType) {
        for (AchVendors vendor : AchVendors.values()) {
            if (vendor.getVendorType()== vendorType) {
                return vendor;
            }
        }
        return null;
    }
    
    public static AchVendors getAchVendorByDesc(String description) {
        if (description == null) return null;
        for (AchVendors vendor : AchVendors.values()) {
            if (vendor.getVendorDesc().equalsIgnoreCase(description.trim())) {
                return vendor;
            }
        }
        return null;
    }
    

    
    public static Map<Integer, String> getAllVendorsMap(String companyId) {
        Map<Integer, String> map = new HashMap<>();
        for (AchVendors vendor : AchVendors.values()) {
                if (vendor.getCompanyId().toLowerCase().contains(companyId.toLowerCase())) {
                    map.put(vendor.vendorType, vendor.vendorDesc);
                }
        }
        return map;
    }
    

}
