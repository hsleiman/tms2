/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amp.crm.embeddable;

import com.amp.crm.common.StringUpperCaseAdapter;
import com.amp.crm.constants.AddressType;
import com.amp.crm.db.entity.base.customer.Address;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * @author Hoang
 */
@Embeddable
public class AddressData {
    @Transient
    private long customerPk;
    @Transient
    private long addressPk;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String address1;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String address2;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String city;
    @XmlJavaTypeAdapter(StringUpperCaseAdapter.class)
    private String state;
    private String zip;
    private AddressType addressType;

    public long getCustomerPk() {
        return customerPk;
    }

    public void setCustomerPk(long customerPk) {
        this.customerPk = customerPk;
    }

    public long getAddressPk() {
        return addressPk;
    }

    public void setAddressPk(long addressPk) {
        this.addressPk = addressPk;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.address1);
        hash = 37 * hash + Objects.hashCode(this.address2);
        hash = 37 * hash + Objects.hashCode(this.city);
        hash = 37 * hash + Objects.hashCode(this.state);
        hash = 37 * hash + Objects.hashCode(this.zip);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Address other = (Address) obj;
        if (!Objects.equals(this.address1, other.getAddressData().address1)) {
            return false;
        }
        if (!Objects.equals(this.address2, other.getAddressData().address2)) {
            return false;
        }
        if (!Objects.equals(this.city, other.getAddressData().city)) {
            return false;
        }
        if (!Objects.equals(this.state, other.getAddressData().state)) {
            return false;
        }
        if (!Objects.equals(this.zip, other.getAddressData().zip)) {
            return false;
        }
        return true;
    }
}
