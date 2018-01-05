import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';

//services
import {AgentsService} from './agents/agents.service';

import { MaterialImports } from '../shared-modules/app.material';

// components
import { AppComponent } from './app.component';
import { MainComponent } from './main/main.component';
import { AppRoutingModule } from './app.routing';
import { GroupsComponent } from './groups/groups.component';
import { EditComponent } from './groups/dialogs/edit/edit.component';
import { AgentsComponent } from './agents/agents.component';
import { GroupComponent } from './group/group.component';
import { AddMemberComponent } from './group/dialogs/add-member/add-member.component';
import { InboundQueuesComponent } from './inbound-queues/inbound-queues.component';
import { OutboundQueuesComponent } from './outbound-queues/outbound-queues.component';
import { OutboundQueueComponent } from './outbound-queue/outbound-queue.component';
import { DispositionsComponent } from './dispositions/dispositions.component';
import { EditInboundQueueComponent } from './inbound-queues/dialogs/edit/edit-inbound-queue.component';
import { AddOrEditDispositionGroupComponent } from './dispositions/dialogs/addOrEdit-disposition-group/addOrEdit-disposition-group.component';
import { AddComponent } from './groups/dialogs/add/add.component';
import { AddQueueComponent } from './outbound-queues/dialogs/add-queue/add-queue.component';
import { AddInboundQueueComponent } from './inbound-queues/dialogs/add/add-queue.component';
import { InboundQueueComponent } from './inbound-queue/inbound-queue.component';
import { EditQueueComponent } from './outbound-queues/dialogs/edit-queue/edit-queue.component';
import { CallHistoryComponent } from './call-history/call-history.component';
import { DispositionComponent } from './disposition/disposition.component';
import { AddOrEditDispositionComponent } from './disposition/dialogs/addOrEdit-disposition/addOrEdit-disposition.component';
import { CampaignsComponent } from './campaigns/campaigns.component';
import {NavBar} from '../shared-modules/nav-bar/app.module';


@NgModule({
  declarations: [
    AppComponent,
    MainComponent,
    GroupsComponent,
    EditComponent,
    AgentsComponent,
    GroupComponent,
    AddMemberComponent,
    InboundQueuesComponent,
    OutboundQueuesComponent,
    OutboundQueueComponent,
    DispositionsComponent,
    EditInboundQueueComponent,
    AddOrEditDispositionGroupComponent,
    AddOrEditDispositionComponent,
    AddComponent,
    AddQueueComponent,
    AddInboundQueueComponent,
    InboundQueueComponent,
    EditQueueComponent,
    CallHistoryComponent,
    DispositionComponent,
    CampaignsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    MaterialImports,
    NavBar
  ],
  entryComponents: [AddInboundQueueComponent, EditComponent, EditInboundQueueComponent, AddMemberComponent,
    AddOrEditDispositionGroupComponent, AddOrEditDispositionComponent, AddComponent, AddQueueComponent, EditQueueComponent],
  providers: [AgentsService],
  exports: [AppComponent]
})
export class AppModule { }
