import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from '../auth/auth-guard.service';
import { AppComponent } from './app.component';
import { CustomerComponent } from './customer/customer.component';


const appRoutes: Routes = [
    { path: 'main', component: AppComponent, canActivate: [AuthGuardService], children: [{ path: 'account/:id', component: CustomerComponent, canActivate: [AuthGuardService] }] }
];


@NgModule({
    // imports: [RouterModule.forRoot(appRoutes, { enableTracing: true })],
    imports: [RouterModule.forRoot(appRoutes, {})],
    exports: [RouterModule]
})

export class AppRoutingModule { }
