import { BrowserModule, Title } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MaterialImports } from './shared-modules/app.material';

import { HttpClient } from './lib/http-client';
import { HttpHelper } from './lib/http-helpers';

import { AppComponent } from './app.component';

import { AppRoutingModule } from './app.routing';

import { AppModule as TMSAPP } from './tms/app.module';
import { AppModule as AdminAPP } from './admin/app.module';
import { AppModule as MainApp } from './main/app.module';

import { AuthComponent } from './auth/auth.component';
import { LoginComponent } from './auth/login/login.component';
import { AuthGuardService } from './auth/auth-guard.service';
import { AuthService } from './auth/auth.service';
import { ChangePasswordComponent } from './auth/change-password/change-password.component';
import { PageNotFoundModule } from './shared-modules/page-not-found/page-not-found.module';


@NgModule({
  declarations: [
    AppComponent,
    AuthComponent,
    LoginComponent,
    ChangePasswordComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MaterialImports,
    AppRoutingModule,
    TMSAPP,
    AdminAPP,
    MainApp,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    PageNotFoundModule // Module meant as a global 404 route. Keep this module last, otherwise it'l overtake any module containing child routes!
  ],
  providers: [Title, AuthGuardService, AuthService, HttpClient, HttpHelper],
  entryComponents: [ChangePasswordComponent],
  bootstrap: [AppComponent]
})
export class AppModule { }
