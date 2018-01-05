import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuardService } from '../auth/auth-guard.service';

import { MainComponent } from './main/main.component';
import { AppComponent } from './app.component';
import { GroupsComponent } from './groups/groups.component';
import { GroupComponent } from './group/group.component';
import { OutboundQueuesComponent } from './outbound-queues/outbound-queues.component';
import { OutboundQueueComponent } from './outbound-queue/outbound-queue.component';
import { InboundQueuesComponent } from './inbound-queues/inbound-queues.component';
import { InboundQueueComponent} from './inbound-queue/inbound-queue.component';
import { DispositionsComponent } from './dispositions/dispositions.component';
import { DispositionComponent } from './disposition/disposition.component';
import { CampaignsComponent } from './campaigns/campaigns.component';
import { CallHistoryComponent } from './call-history/call-history.component';

const appRoutes: Routes = [{
    path: 'tms', component: AppComponent, children: [
    { path: 'groups', component: GroupsComponent, canActivate: [ AuthGuardService ] },
    { path: 'group/:id', component: GroupComponent, canActivate: [ AuthGuardService ] },
    { path: 'outbounds', component: OutboundQueuesComponent, canActivate: [ AuthGuardService ] },
    { path: 'outbound/:id', component: OutboundQueueComponent, canActivate: [ AuthGuardService ] },
    { path: 'inbounds', component: InboundQueuesComponent, canActivate: [ AuthGuardService ] },
    { path: 'inbound/:id', component: InboundQueueComponent, canActivate: [ AuthGuardService ] },
    { path: 'dispositions', component: DispositionsComponent, canActivate: [ AuthGuardService ]},
    { path: 'disposition/:id', component: DispositionComponent, canActivate: [ AuthGuardService ]},
    { path: 'campaigns', component: CampaignsComponent, canActivate: [ AuthGuardService ]},
    { path: 'call-history', component: CallHistoryComponent, canActivate: [ AuthGuardService ]}
]
}];


@NgModule({
    imports: [RouterModule.forChild(appRoutes)],
    exports: [RouterModule]
})

export class AppRoutingModule { }
