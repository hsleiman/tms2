import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';

import {AgentsService} from './agents/agents.service';

import { MaterialImports } from '../shared-modules/app.material';

// components
import { AppComponent } from './app.component';
import { MainComponent } from './main/main.component';
import { AppRoutingModule } from './app.routing';
import { QaComponent } from './qa/qa.component';
import { AgentsComponent } from './agents/agents.component';
import { RolesComponent } from './roles/roles.component';
import { AddAgentComponent } from './agents/dialogs/add-agent/add-agent.component';
import { PermissionRolesComponent } from './permission-roles/permission-roles.component';
import { RolesPermissionsComponent } from './roles-permissions/roles-permissions.component';
import { AgentDetailsComponent } from './agents/agent-details/agent-details.component';



@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    QaComponent,
    AgentsComponent,
    RolesComponent,
    PermissionRolesComponent,
    RolesPermissionsComponent,
    AddAgentComponent,
    AgentDetailsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    MaterialImports
  ],
  entryComponents: [AddAgentComponent],
  providers: [AgentsService],
  exports:[AppComponent]
})
export class AppModule { }
