import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';
import { MatDialog, MatDialogConfig } from '@angular/material';
import { ChangePasswordComponent } from '../auth/change-password/change-password.component';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(private authService: AuthService, private router: Router, private matDialog: MatDialog ){}
  title = 'app';

  logout() {
    this.authService.logout();
    this.router.navigate(['login']);
  }

  changePassword() {
    let dialogSettings: MatDialogConfig = {
      disableClose: true
    }
    let changePasswordDialog = this.matDialog.open(ChangePasswordComponent, dialogSettings);
  }
}
