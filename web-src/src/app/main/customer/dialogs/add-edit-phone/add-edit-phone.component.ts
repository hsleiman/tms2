import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { CustomerPhoneVM } from '../../../../shared-modules/main/viewmodels/CustomerPhoneVM';
import { FormGroup, FormControl } from '@angular/forms';
import { PhoneTypeKeyValueArray } from '../../../../shared-modules/main/enums/PhoneType';
import { PhoneSourceKeyValueArray } from '../../../../shared-modules/main/enums/PhoneSource';
import { PhoneSpecialInstructionKeyValueArray } from '../../../../shared-modules/main/enums/PhoneSpecialInstruction';

@Component({
  selector: 'app-add-edit-phone',
  templateUrl: './add-edit-phone.component.html',
  styleUrls: ['./add-edit-phone.component.scss']
})
export class AddEditPhoneComponent implements OnInit {

  dialogTitle: string;
  addEditPhoneForm: FormGroup;

  phoneTypes = PhoneTypeKeyValueArray;
  phoneSources = PhoneSourceKeyValueArray;
  phoneInstructions = PhoneSpecialInstructionKeyValueArray;

  constructor(private dialogRef: MatDialogRef<AddEditPhoneComponent>, @Inject(MAT_DIALOG_DATA) private oldData: CustomerPhoneVM) {
    this.init();
  }

  init(): void {
    if (this.oldData.phonePk == null) {
      this.dialogTitle = "Add New Phone";
    }
    else {
      this.dialogTitle = "Edit Phone";
    }

    this.addEditPhoneForm = new FormGroup({
      phoneNumber: new FormControl(null, []),
      phoneNumberType: new FormControl(null, []),
      phoneLabel: new FormControl(null, []),
      phoneSource: new FormControl(null, []),
      specialInstr: new FormControl(null, []),
      notes: new FormControl(null, [])
    });
  }

  ngOnInit() {
    if (this.oldData.phonePk != null) {
      this.addEditPhoneForm.patchValue(this.oldData); /* Patch value if nulls can exist, set otherwise. */
    }
  }

  savePhone(): void {
    let newPhone = <CustomerPhoneVM>this.addEditPhoneForm.value;
    console.log(newPhone);
    //this.dialogRef.close(newPhone);
  }

}
