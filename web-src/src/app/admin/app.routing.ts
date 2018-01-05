import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from '../auth/auth-guard.service';

import { MainComponent } from './main/main.component';
import { AppComponent } from './app.component';

import { AgentsComponent } from './agents/agents.component';
import { PermissionRolesComponent } from './permission-roles/permission-roles.component';
import { RolesPermissionsComponent } from './roles-permissions/roles-permissions.component';
import { AgentDetailsComponent } from './agents/agent-details/agent-details.component';


const appRoutes: Routes = [
    {
        path: 'admin',
        component: AppComponent,
        canActivate: [AuthGuardService],
        children: [
            {
                path: 'agents',
                component: AgentsComponent
            },
            {
                path: 'agents/:id',
                component: AgentDetailsComponent
            },
            {
                path: 'permissions-roles',
                component: PermissionRolesComponent
            },
            {
                path: 'roles-permissions',
                component: RolesPermissionsComponent
            }
        ]
    }
];


@NgModule({
    imports: [RouterModule.forChild(appRoutes)],
    exports: [RouterModule]
})

export class AppRoutingModule { }
