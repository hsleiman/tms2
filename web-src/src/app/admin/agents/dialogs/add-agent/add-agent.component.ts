import { Component } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { AgentsService, AgentI } from '../../agents.service';

@Component({
  selector: 'app-add',
  templateUrl: './add-agent.component.html',
  styleUrls: ['./add-agent.component.css'],
  providers: [ AgentsService ]
})
export class AddAgentComponent  {

  public data = new AgentI();

  constructor(public dialogRef: MatDialogRef<AddAgentComponent>, private agentsService: AgentsService) { }

  addGroup() {
    console.log(this.data);
    this.agentsService.addAgent(this.data).then((result) => {
      console.log(result);
      this.dialogRef.close(result);
    });
  }

  onNoClick() {
    this.dialogRef.close();
  }

}
