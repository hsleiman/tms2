import { BrowserModule, Title } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MaterialImports } from '../shared-modules/app.material';

import { AppRoutingModule } from './app.routing';

import { AppComponent } from './app.component';
import { CustomerComponent } from './customer/customer.component';
import { CreditorsComponent } from './creditors/creditors.component';
import { CustomerInfoComponent } from './customer/customer-info/customer-info.component';
import { ContactsComponent } from './customer/contacts/contacts.component';
import { MilestoneComponent } from './customer/milestone/milestone.component';
import { DebtComponent } from './customer/debt/debt.component';
import { PaymentHistoryComponent } from './customer/payment-history/payment-history.component';
import { LogComponent } from './customer/log/log.component';
import { AuthGuardService } from '../auth/auth-guard.service';
import { AuthService } from '../auth/auth.service';
import { AddEditPhoneComponent } from './customer/dialogs/add-edit-phone/add-edit-phone.component';
import { AddEditEmailComponent } from './customer/dialogs/add-edit-email/add-edit-email.component';


@NgModule({
  declarations: [
    AppComponent,
    CustomerComponent,
    CreditorsComponent,
    CustomerInfoComponent,
    ContactsComponent,
    MilestoneComponent,
    DebtComponent,
    PaymentHistoryComponent,
    LogComponent,
    AddEditPhoneComponent,
    AddEditEmailComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MaterialImports,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule
  ],
  providers: [Title],
  entryComponents: [AddEditEmailComponent, AddEditPhoneComponent],
  exports: [AppComponent]
})
export class AppModule { }
