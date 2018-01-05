import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators, ValidatorFn, AsyncValidatorFn, AbstractControl } from '@angular/forms';
import { MatDialogRef, MatSnackBar } from '@angular/material';
import { AuthService } from '../auth.service';
import { AgentsService, ChangePasswordVM } from '../../admin/agents/agents.service';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss'],
  providers: [AuthService, AgentsService]
})
export class ChangePasswordComponent implements OnInit {
  checkingOldPassword: boolean = false;
  username: string;
  changePasswordForm: FormGroup;

  constructor(private snackbar: MatSnackBar, private dialogRef: MatDialogRef<ChangePasswordComponent>,
    private authService: AuthService, private agentsService: AgentsService) {
    this.init();
  }

  init(): void {
    this.changePasswordForm = new FormGroup({
      password: new FormControl(""),
      newPassword: new FormControl(""),
      newPasswordConfirm: new FormControl("")
    });
  }

  ngOnInit() {
    this.username = this.authService.getLoggedInUsername();
    this.changePasswordForm.controls["password"].setValidators([Validators.required]);
    this.changePasswordForm.controls["password"].setAsyncValidators([this.oldPasswordValidator()]);
    this.changePasswordForm.controls["newPassword"].setValidators([Validators.required, this.differentFromOldValidator(), this.newAndConfirmMatchValidator()]);
    this.changePasswordForm.controls["newPasswordConfirm"].setValidators([Validators.required, this.differentFromOldValidator(), this.newAndConfirmMatchValidator()]);
  }

  oldPasswordValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Promise<{[key: string]: any}> => {
      if (control.value == null || control.value == "") {
        return null;
      }
      this.checkingOldPassword = true;
      let credentials: ChangePasswordVM = {
        agentUsername: this.authService.getLoggedInUsername(),
        agentPassword: control.value,
        newAgentPassword: null
      };
      return this.agentsService.confirmPassword(credentials).then(data => {
        this.checkingOldPassword = false;
        if (data === true) {
          return null;
        }
        return {"oldPasswordMismatch": {value: control.value}};
      }, err => {
        this.checkingOldPassword = false;
        return null;
      });
    }
  }

  differentFromOldValidator(): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} => {
      let oldValue = this.changePasswordForm.controls["password"].value;
      let value = control.value;

      let valuesNotFilled = oldValue == null || oldValue == "" || value == null || value == "";
      if (valuesNotFilled) {
        return null;
      }

      let controlsValuesMatch = value === oldValue;
      if (controlsValuesMatch) {
        return {"sameAsOldPassword": {value: value}};
      }
      return null;
    }
  }

  newAndConfirmMatchValidator(): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} => {
      let newValue = this.changePasswordForm.controls["newPassword"].value;
      let confirmValue = this.changePasswordForm.controls["newPasswordConfirm"].value;

      let valuesNotFilled = newValue == null || newValue == "" || confirmValue == null || confirmValue == "";
      if (valuesNotFilled) {
        return null;
      }

      let controlsValuesMatch = newValue === confirmValue;
      if (controlsValuesMatch) {
        return null;
      }
      return {"newAndConfirmDontMatch": {value: control.value}};
    }
  }

  invalidPasswordMessage(): string {
    let fc = this.changePasswordForm.controls["password"];
    if (fc.invalid) {
      let invalidReasons = fc.errors;
      if (invalidReasons["required"] != null) {
        return "Please enter your password.";
      }
      if (invalidReasons["oldPasswordMismatch"] != null) {
        return "Password doesn't match our records.";
      }
    }
    return "";
  }

  invalidNewPasswordMessage(): string {
    let fc = this.changePasswordForm.controls["newPassword"];
    if (fc.invalid) {
      let invalidReasons = fc.errors;
      if (invalidReasons["required"] != null) {
        return "Please enter a new password.";
      }
      if (invalidReasons["sameAsOldPassword"] != null) {
        return "Please enter a different password than your current password.";
      }
      if (invalidReasons["newAndConfirmDontMatch"] != null) {
        return "Please match this field with the confirm new password field.";
      }
    }
    return "";
  }

  invalidNewPasswordConfirmMessage(): string {
    let fc = this.changePasswordForm.controls["newPasswordConfirm"];
    if (fc.invalid) {
      let invalidReasons = fc.errors;
      if (invalidReasons["required"] != null) {
        return "Please confirm your new password.";
      }
      if (invalidReasons["sameAsOldPassword"] != null) {
        return "Please enter a different password than your current password.";
      }
      if (invalidReasons["newAndConfirmDontMatch"] != null) {
        return "Please match this field with the new password field.";
      }
    }
    return "";
  }

  confirm(): void {
    let credentials: ChangePasswordVM = {
      agentUsername: this.username,
      agentPassword: this.changePasswordForm.controls["password"].value,
      newAgentPassword: this.changePasswordForm.controls["newPassword"].value
    };
    this.agentsService.changePassword(credentials).then(data => {
      if (data) {
        this.snackbar.open("Changed password successfully.", null, {duration: 3000});
        this.dialogRef.close(data);
      } else {
        this.snackbar.open("Failed to change password.", null, {duration: 3000});
      }
    }, err => {
      this.snackbar.open("Failed to change password.", null, {duration: 3000});
    });
  }

  

}
