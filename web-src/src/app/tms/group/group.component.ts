import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { MatTableDataSource, MatDialog } from '@angular/material';

import { GroupService, GroupI } from './group.service';
import { AgentsService } from '../agents/agents.service';
import { GroupsService } from '../groups/groups.service';
import { TmsAgentI } from '../agents/agents.service';
import { AddMemberComponent } from './dialogs/add-member/add-member.component';
import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'app-group',
  templateUrl: './group.component.html',
  styleUrls: ['./group.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [GroupService, GroupsService]
})
export class GroupComponent implements OnInit {

  public navParams;
  private groupId:number;
  private groupInfo = new GroupI();
  public agents: Array<TmsAgentI>;
  public errorNotFound = false;
  public dialerGroupAgents = new Array<TmsAgentI>();

  constructor(private route: ActivatedRoute,
    private router: Router,
    private groupService: GroupService,
    private groupsService: GroupsService,
    private agentsService: AgentsService,
    public dialog: MatDialog) { }

  ngOnInit() {
    this.groupId = Number.parseInt(this.route.snapshot.paramMap.get('id'));
    if (this.groupId && typeof this.groupId === 'number') {
      this.groupService.getGroupById(this.groupId).then((result) => {
        this.groupService.getAgentsForGroup(this.groupId).then((agentResult) => {
          this.dialerGroupAgents = agentResult;
        });
        this.groupInfo = result;
        this.navParams = [{ name: 'Group', route: '/tms/groups' }, { name: result.groupName, route: `/tms/group/${this.groupId}` }];
      });
    } else {
      this.errorNotFound = true;
    }
  }

  updatePiorityAndWieght(){
    this.groupService.updateAgentToGroup(this.groupId, this.dialerGroupAgents).then((result)=>{
      
    });
  }

  addAgent() {
    const dialogRef = this.dialog.open(AddMemberComponent, {
      width: '600px',
      data: { agents: this.dialerGroupAgents, groupId: this.route.snapshot.paramMap.get('id') }
    });

    dialogRef.afterClosed().subscribe((result: Array<TmsAgentI>) => {
      this.ngOnInit();
    });
  }

  remove(agent) {
    console.log(agent);
    this.groupService.deleteAgentToGroup(this.route.snapshot.paramMap.get('id'), agent).then((result) => {
      this.ngOnInit();
    });

  }
}
