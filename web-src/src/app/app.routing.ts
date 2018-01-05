import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from './auth/auth-guard.service';

import { AppComponent } from './tms/app.component';
import { AppComponent as AdminComponent } from './admin/app.component';
import { AppComponent as MainApp } from './main/app.component';
import { LoginComponent } from './auth/login/login.component';

const appRoutes: Routes = [
    { path: 'main', component: MainApp, canActivate: [AuthGuardService] },
    { path: 'tms', component: AppComponent, canActivate: [AuthGuardService] },
    { path: 'admin', component: AdminComponent, canActivate: [AuthGuardService] },
    { path: 'login', component: LoginComponent },
    {path:'', redirectTo:'main', pathMatch:'full'}
];


@NgModule({
    // imports: [RouterModule.forRoot(appRoutes, { enableTracing: true })],
    imports: [RouterModule.forRoot(appRoutes, {})],
    exports: [RouterModule]
})

export class AppRoutingModule { }
