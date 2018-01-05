import { Component, OnInit, Inject } from '@angular/core';
import { CustomerEmailVM } from '../../../../shared-modules/main/viewmodels/CustomerEmailVM';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormGroup, FormControl } from '@angular/forms';
import { EmailAddressTypeKeyValueArray } from '../../../../shared-modules/main/enums/EmailAddressType';

@Component({
  selector: 'app-add-edit-email',
  templateUrl: './add-edit-email.component.html',
  styleUrls: ['./add-edit-email.component.scss']
})
export class AddEditEmailComponent implements OnInit {

  dialogTitle: string;
  addEditEmailForm: FormGroup;

  emailTypes = EmailAddressTypeKeyValueArray;

  constructor(private dialogRef: MatDialogRef<AddEditEmailComponent>, @Inject(MAT_DIALOG_DATA) private oldData: CustomerEmailVM) {
    this.init();
  }

  init(): void {
    if (this.oldData.emailPk == null) {
      this.dialogTitle = "Add New Email";
    }
    else {
      this.dialogTitle = "Edit Email";
    }

    this.addEditEmailForm = new FormGroup({
      emailAddress: new FormControl(null, []),
      emailAddressType: new FormControl(null, []),
      doNotContact: new FormControl(null, []),
      emailAddressBad: new FormControl(null, [])
    });
  }

  ngOnInit() {
    if (this.oldData.emailPk != null) {
      this.addEditEmailForm.patchValue(this.oldData);
    }
  }

  saveEmail(): void {
    let newEmail = <CustomerEmailVM>this.addEditEmailForm.value;
    console.log(newEmail);
    //this.dialogRef.close(newEmail);
  }

}
