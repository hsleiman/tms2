/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.db.entity.utility;

import com.amp.crm.db.entity.superentity.SuperEntity;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.hibernate.annotations.Index;

/**
 *
 * @author David
 */
    @NamedQueries({
    @NamedQuery(
            name="ZipTimezone.GetRecordsByZip",
            query="SELECT x FROM ZipTimezone x where x.zip =:Zip"
    ),
    @NamedQuery(
            name="ZipTimezone.getRecordsByCity",
            query="Select x From ZipTimezone x where x.city =:City"
    ),
    @NamedQuery(
            name="ZipTimezone.getRecordsByAreaCode",
            query="Select x From ZipTimezone x where x.areacode=:AreaCode"
    )
})

@Entity
@Table(schema = "sti")
public class ZipTimezone extends SuperEntity {
    @Index(name="zipIndex")
    @Column(length = 5)
    private String zip;
    
    @Column(length = 1)
    private String zipCodeType;    
    
    private Double y_coord;
    
    private Double x_coord;
  
    private String city;
    
    @Column(length = 1)   
    private String cityType;
    
    private Integer state_fips;
    
    @Column(length = 2)
    private String state;
    
    private String statename;
    
    private Integer county_fips;
    
    private String countynm;
    
    private Integer msa;
    
    private Integer areacode;
    
    private String timezone;
    
    private Integer gmtoffset;
    
    @Column(length = 1)
    private String dst;
    

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Double getY_coord() {
        return y_coord;
    }

    public void setY_coord(Double y_coord) {
        this.y_coord = y_coord;
    }

    public Double getX_coord() {
        return x_coord;
    }

    public void setX_coord(Double x_coord) {
        this.x_coord = x_coord;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getState_fips() {
        return state_fips;
    }

    public void setState_fips(Integer state_fips) {
        this.state_fips = state_fips;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatename() {
        return statename;
    }

    public void setStatename(String statename) {
        this.statename = statename;
    }

    public Integer getCounty_fips() {
        return county_fips;
    }

    public void setCounty_fips(Integer county_fips) {
        this.county_fips = county_fips;
    }

    public String getCountynm() {
        return countynm;
    }

    public void setCountynm(String countynm) {
        this.countynm = countynm;
    }

    public Integer getMsa() {
        return msa;
    }

    public void setMsa(Integer msa) {
        this.msa = msa;
    }

    public Integer getAreacode() {
        return areacode;
    }

    public void setAreacode(Integer areacode) {
        this.areacode = areacode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getGmtoffset() {
        return gmtoffset;
    }

    public void setGmtoffset(Integer gmtoffset) {
        this.gmtoffset = gmtoffset;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getZipCodeType() {
        return zipCodeType;
    }

    public void setZipCodeType(String zipCodeType) {
        this.zipCodeType = zipCodeType;
    }

    public String getCityType() {
        return cityType;
    }

    public void setCityType(String cityType) {
        this.cityType = cityType;
    }
    
    @Override
    public boolean equals(Object other){
        if(other == null || this==null){return false;}
        if(other.getClass().isInstance(this)){
            ZipTimezone otherZipTimezone = (ZipTimezone)other;
            return (super.getPk() == otherZipTimezone.getPk() );
        }else{
            return false;
        }
    }
    
    @Override
    public int hashCode(){
        int hash = 3;
        hash = 101 * hash + Objects.hashCode(this.areacode);
        hash = 101 * hash + Objects.hashCode(this.city);
        hash = 101 * hash + Objects.hashCode(this.countynm);
        hash = 101 * hash + Objects.hashCode(this.state);
        hash = 101 * hash + Objects.hashCode(this.x_coord);
        hash = 101 * hash + Objects.hashCode(this.y_coord);
        hash = 101 * hash + Objects.hashCode(this.zip);
        hash = 101 * hash + Objects.hashCode(this.zipCodeType);
        hash = 101 * hash + Objects.hashCode(this.timezone);
        hash = 101 * hash + Objects.hashCode(this.statename);
        return hash;
    }
}
