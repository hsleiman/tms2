import { Component, OnInit } from '@angular/core';

import { LogComponent } from './log/log.component';
import { CustomerInfoComponent } from './customer-info/customer-info.component';
import { PaymentHistoryComponent } from './payment-history/payment-history.component';
import { MilestoneComponent } from './milestone/milestone.component';
import { CustomerPhoneVM } from '../../shared-modules/main/viewmodels/CustomerPhoneVM';
import { CustomerEmailVM } from '../../shared-modules/main/viewmodels/CustomerEmailVM';
import { MatDialog, MatDialogConfig } from '@angular/material';
import { AddEditPhoneComponent } from './dialogs/add-edit-phone/add-edit-phone.component';
import { AddEditEmailComponent } from './dialogs/add-edit-email/add-edit-email.component';

@Component({
  selector: 'app-customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.css']
})
export class CustomerComponent implements OnInit {

  constructor(private matDialog: MatDialog) { }

  ngOnInit() {
  }

  /* If adding a button onto an expansion panel's header, bind it's click event to this function so
     clicking the button doesn't open/close the panel as well. */
  openPanelMenu(event: Event): void {
    event.stopPropagation();
  }

  openAddEditPhonePopup(phone: CustomerPhoneVM): void {
    if (phone == null) {
      phone = new CustomerPhoneVM();
    }
    if (phone.customerPk == null) {

    }
    if (phone.accountPk == null) {

    }
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

  openAddEditEmailPopup(email: CustomerEmailVM): void {
    if (email == null) {
      email = new CustomerEmailVM();
    }
    if (email.customerPk == null) {

    }
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
