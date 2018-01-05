import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { AgentsService, AgentI } from './agents.service';
import { AddAgentComponent } from './dialogs/add-agent/add-agent.component';
import { MatTableDataSource, MatDialog, MatDialogConfig } from '@angular/material';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [AgentsService]
})
export class AgentsComponent implements OnInit {

  constructor(private agentService: AgentsService, public dialog: MatDialog) { }
  public agents: Array<AgentI>;
  dataSource: any;
  displayedColumns = ['pk', 'name', 'userName'];
  ngOnInit() {
    this.agentService.getAgents().then((agentsPrimise: Array<AgentI>) => {
      this.agents = agentsPrimise;
      this.dataSource = new MatTableDataSource(this.agents);
    });
  }

  addAgent() {
    const dialogRef = this.dialog.open(AddAgentComponent, {
      width: '500px',
      height: '600px',
      data: ''
    });

    dialogRef.afterClosed().subscribe((result: Array<AgentI>) => {
      if (result && result.length > 0) {
        result.forEach(agent => {
          this.agents.push(agent);
        });
      }
    });
  }
}
