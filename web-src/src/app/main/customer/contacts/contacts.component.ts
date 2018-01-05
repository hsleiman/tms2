import { Component, OnInit } from '@angular/core';
import { CustomerContactInfo, CustomerService } from '../../customer/customer.service';
import { getPhoneSpecialInstructionLabel } from '../../../shared-modules/main/enums/PhoneSpecialInstruction';
import { getPhoneSourceLabel } from '../../../shared-modules/main/enums/PhoneSource';
import { getPhoneTypeLabel } from '../../../shared-modules/main/enums/PhoneType';
import { getEmailAddressTypeLabel } from '../../../shared-modules/main/enums/EmailAddressType';
import { CustomerPhoneVM } from '../../../shared-modules/main/viewmodels/CustomerPhoneVM';
import { CustomerEmailVM } from '../../../shared-modules/main/viewmodels/CustomerEmailVM';
import { MatDialogConfig, MatDialog } from '@angular/material';
import { AddEditPhoneComponent } from '../dialogs/add-edit-phone/add-edit-phone.component';
import { AddEditEmailComponent } from '../dialogs/add-edit-email/add-edit-email.component';

@Component({
    selector: 'app-contacts',
    templateUrl: './contacts.component.html',
    styleUrls: ['./contacts.component.css'],
    providers: [CustomerService]
})
export class ContactsComponent implements OnInit {
    contactInfo: CustomerContactInfo;

    constructor(private matDialog: MatDialog, private mainService: CustomerService) { }

    ngOnInit() {
        this.contactInfo = this.mainService.getContactInfoForCustomer();
    }

    getPhoneTypeLabel(phone: CustomerPhoneVM): string {
        return getPhoneTypeLabel(phone.phoneNumberType);
    }

    getFormattedPhone(phone: CustomerPhoneVM): string {
        let stringifiedNumber = phone.phoneNumber.toString(10);
        while (stringifiedNumber.length < 7) { // In the off chance that a phone number starts with zero(s), pad it/them in.
            stringifiedNumber = "0" + stringifiedNumber;
        }
        return (stringifiedNumber.substring(0, 3) + "-" + stringifiedNumber.substring(3, 6) + "-" + stringifiedNumber.substring(6));
    }

    getPhoneSourceLabel(phone: CustomerPhoneVM): string {
        return getPhoneSourceLabel(phone.phoneSource);
    }

    getPhoneSpecialInstructionsLabel(phone: CustomerPhoneVM): string {
        return getPhoneSpecialInstructionLabel(phone.specialInstr);
    }

    getEmailAddressTypeLabel(email: CustomerEmailVM): string {
        return getEmailAddressTypeLabel(email.emailAddressType);
    }

    editPhone(phone: CustomerPhoneVM): void {
        let popupOptions: MatDialogConfig = {
            data: phone,
            disableClose: true,
        };
        let popup = this.matDialog.open(AddEditPhoneComponent, popupOptions);

        popup.afterClosed().subscribe(data => {
            if (data != null) {
                // Refresh contact information to show the new/changed contact info.
            }
        });
    }

    editEmail(email: CustomerEmailVM): void {
        let popupOptions: MatDialogConfig = {
            data: email,
            disableClose: true,
        };
        let popup = this.matDialog.open(AddEditEmailComponent, popupOptions);

        popup.afterClosed().subscribe(data => {
            if (data != null) {
                // Refresh contact information to show the new/changed contact info.
            }
        });
    }

}
