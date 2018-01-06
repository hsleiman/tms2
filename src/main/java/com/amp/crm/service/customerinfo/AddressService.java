/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.crm.service.customerinfo;

import com.amp.crm.constants.AddressType;
import com.amp.crm.db.entity.base.customer.Customer;
import com.amp.crm.db.entity.base.customer.Address;
import com.amp.crm.db.repository.customer.CustomerRepository;
import com.amp.crm.db.repository.customerinfo.AddressRepository;
import com.amp.crm.embeddable.AddressData;
import com.amp.crm.exception.StiException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Bishistha
 */
@Service
@Transactional
public class AddressService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private AddressRepository addressRepo;
    
    @Autowired
    private CustomerRepository customerRepo;
    
    public long createOrUpdateAddressForCustomer(AddressData addressData, long customerPk) {
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        if(customer != null){
            if(addressData.getAddressPk() <= 0){
                Address newAddress = new Address();
                newAddress.setAddressData(addressData);
                entityManager.persist(newAddress);
                customer.getAddress().add(newAddress);
                newAddress.setCustomer(customer);
                return newAddress.getPk();
            } else {
                Address oldAddress = addressRepo.findAddressByPk(addressData.getAddressPk());
                oldAddress.setAddressData(addressData);
                addressRepo.mergeAddress(oldAddress);
                return addressData.getAddressPk();
            }
        }
        return 0;
    }
    
    public List<AddressData> getAddressForCustomer(long customerPk) {
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        ArrayList<AddressData> addressDataList = new ArrayList<>();
        for(Address address : customer.getAddress()){
            addressDataList.add(address.getAddressData());
        }
        return addressDataList;
    }
    

    
    public AddressData getAddressForCustomerByType(long customerPk, AddressType type) throws StiException{
        Customer customer = customerRepo.findCustomerByPk(customerPk);
        Set<Address> addresses = customer.getAddress();
        Address retAddress = null;
        for(Address address: addresses){
            if(address.getAddressData().getAddressType() == type){
                retAddress =  address;
            }
        }
        return retAddress.getAddressData();
    }
}
