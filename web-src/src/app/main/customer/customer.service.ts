import { Injectable } from '@angular/core';
import { Config } from '../../app.config';
import { CustomerContactInfoVM } from '../../shared-modules/main/viewmodels/CustomerContactInfoVM';
import { HttpClient } from '../../lib/http-client';
import { CustomerPhoneVM } from '../../shared-modules/main/viewmodels/CustomerPhoneVM';
import { CustomerEmailVM } from '../../shared-modules/main/viewmodels/CustomerEmailVM';

@Injectable()
export class CustomerService {

  serviceUrl: string = Config.Base_URL + "/settleitRestController";

  constructor(private httpClient: HttpClient) { }


  getContactInfoForCustomer(): CustomerContactInfo {
    return formMockContactInfo();
  }

  getCustomerContactInformation(customerPk: number): Promise<CustomerContactInfoVM> {
    let url = this.serviceUrl + "/getCustomerContactInformation/" + customerPk.toString(10);
    return this.httpClient.get(url, null);
  }

  createOrUpdateCustomerPhone(phone: CustomerPhoneVM): Promise<number> {
    let url = this.serviceUrl + "/createOrUpdateCustomerPhone";
    return this.httpClient.post(url, phone, null);
  }

  createOrUpdateCustomerEmail(email: CustomerEmailVM): Promise<number> {
    let url = this.serviceUrl + "/createOrUpdateCustomerEmail";
    return this.httpClient.post(url, email, null);
  }

  getLogsForCustomer(): Array<Log> {
    return formMockLogs();
  }
}

export class Log {
  flagged: boolean;
  creationDate: Date;
  logType: number;
  employeeId: string;
  notes: string;
}

function formMockLogs(): Array<Log> {
  let mockLogs = new Array<Log>();
  for (let i = 0; i < 10; i++) {
    let mockLog = new Log();
    mockLog.flagged = Math.random() <= 0.5;
    mockLog.creationDate = new Date(Math.random() * Date.now());
    mockLog.logType = Math.floor(Math.random() * 4);
    mockLog.employeeId = "";
    for (let i = 0; i < 12; i++) {
      mockLog.employeeId += formRandomAlphaNumeric();
    }
    mockLog.notes = "";
    for (let i = 0; i < 20; i++) {
      mockLog.notes += formRandomAlphaNumeric();
    }
    mockLogs.push(mockLog);
  }
  return mockLogs;
}

export class CustomerContactInfo {
  verbalCandD: boolean;
  blockCallerId: boolean;
  useLocalNumber: boolean;
  phones: Array<CustomerPhoneVM>;
  emailAddresses: Array<CustomerEmailVM>;
}

function formMockContactInfo(): CustomerContactInfo {
  let mockContactInfo = new CustomerContactInfo();
  mockContactInfo.verbalCandD = Math.random() <= 0.5;
  mockContactInfo.blockCallerId = Math.random() <= 0.5;
  mockContactInfo.useLocalNumber = Math.random() <= 0.5;
  mockContactInfo.phones = new Array<CustomerPhoneVM>();
  for (let i = 0; i < 1 + (Math.floor(Math.random() * 3)); i++) {
    mockContactInfo.phones.push(formMockPhone());
  }
  mockContactInfo.emailAddresses = new Array<CustomerEmailVM>();
  for (let i = 0; i < 1 + (Math.floor(Math.random() * 3)); i++) {
    mockContactInfo.emailAddresses.push(formMockEmailAddress());
  }
  return mockContactInfo;
}


function formMockPhone(): CustomerPhoneVM {
  let mockPhone = new CustomerPhoneVM();
  mockPhone.phonePk = Math.floor(Math.random() * 10000000000);
  mockPhone.phoneNumber = Math.floor(Math.random() * 8999999999) + 1000000000;
  mockPhone.phoneNumberType = Math.floor(Math.random() * 2);
  mockPhone.phoneSource = Math.floor(Math.random() * 5);
  mockPhone.specialInstr = Math.floor(Math.random() * 5);
  return mockPhone;
}

function formMockEmailAddress(): CustomerEmailVM {
  let mockAddress = new CustomerEmailVM();
  mockAddress.emailPk = Math.floor(Math.random() * 1000000000);
  mockAddress.emailAddress = "";
  for (let i = 0; i < 15; i++) {
    mockAddress.emailAddress += formRandomAlphaNumeric();
  }
  mockAddress.emailAddress += "@mockedaddress.inc";
  mockAddress.emailAddressType = Math.floor(Math.random() * 2);
  return mockAddress;
}

function formRandomAlpha(): string {
  let pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  let rnd = Math.floor(Math.random() * pool.length);
  return pool.charAt(rnd);
}

function formRandomDigit(): string {
  let rnd = Math.floor(Math.random() * 10);
  return rnd.toString(10);
}

function formRandomAlphaNumeric(): string {
  if (Math.random() <= 0.5) {
    return formRandomAlpha();
  }
  return formRandomDigit();
}