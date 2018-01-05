import { Component, OnInit, ViewEncapsulation, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatAutocompleteSelectedEvent } from '@angular/material';
import { TmsAgentI, AgentsService } from '../../../agents/agents.service';
import { GroupService } from '../../group.service';
import { FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';

@Component({
  selector: 'app-add-member',
  templateUrl: './add-member.component.html',
  styleUrls: ['./add-member.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [GroupService]
})
export class AddMemberComponent implements OnInit {
  private text = '';
  myControl: FormControl = new FormControl();
  filteredOptions: Observable<any[]>;
  selectedAgents: Array<TmsAgentI> = [];
  constructor(public dialogRef: MatDialogRef<AddMemberComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AddMemberI, public agentService: AgentsService, private groupService: GroupService) { }
  private agents: Array<TmsAgentI>;
  ngOnInit() {
    this.agentService.getAgents().then((promiseAgents) => {
      this.agents = promiseAgents;
      this.data.agents.forEach((agent: TmsAgentI) => {
        const index = this.findIndexOfAgent(agent.phoneExtension, this.agents);
        this.removeAgentFromList(index);
      });
    });

    this.filteredOptions = this.myControl.valueChanges
      .startWith('')
      .map(agent => this.filter(agent));
  }

  filter(val: string): TmsAgentI[] {
    if (val) {
      const commaVal = val.split(',');
      if (commaVal.length > 1) {
        val = commaVal[commaVal.length - 1];
      }
      return this.agents.filter((agent) => {
        if (agent.firstName && agent.firstName)
          return agent.firstName.toLowerCase().indexOf(val.toLocaleLowerCase()) === 0
      });
    }
  }

  findIndexOfAgent(extension: number, list: Array<TmsAgentI>) {
    return list.findIndex((groupAgent: TmsAgentI) => {
      return (groupAgent.phoneExtension === extension);
    });
  }

  removeAgentFromList(index) {
    if (typeof index === 'number') {
      this.agents.splice(index, 1);
    }
  }

  deleteMemeber(extension) {
    const index = this.findIndexOfAgent(extension, this.selectedAgents);
    this.agents.push(this.selectedAgents[index]);
    this.selectedAgents.splice(index, 1);
  }

  displayFn(agent: TmsAgentI): string {
    if (agent && agent.firstName) {
      return agent.firstName ? `${agent.firstName} ${agent.lastName}` : '';
    }
  }

  selected(agent: TmsAgentI) {
    this.selectedAgents.push(agent);
    this.renderView(this.selectedAgents);
  }

  renderView(agent: Array<TmsAgentI>) {
    let view = '';
    agent.forEach(selectedAget => {
      view = view + selectedAget.firstName + ' ' + selectedAget.lastName + ',';
    });
    this.text = view;
    this.myControl.setValue(view);
  }

  onNoClick() {
    this.dialogRef.close();
  }

  selectAgents() {
    let selectegAgents = '';
    let duplicateAgents = [];
    this.selectedAgents.forEach((agent: TmsAgentI) => {
      let count = 0;
      this.selectedAgents.forEach((agent2) => {
        if (agent.phoneExtension === agent2.phoneExtension)
          count++;
      });

      if (count === 1) {
        selectegAgents = selectegAgents + agent.userName + ',';
      }
      else if (count > 1) {
        let dupCount = 0;
        duplicateAgents.forEach((dupAgent) => {
          if (dupAgent.phoneExtension === agent.phoneExtension) {
            dupCount++;
          }
        });

        if (dupCount === 0) {
          duplicateAgents.push(agent);
        }
      }
    });

    duplicateAgents.forEach((agent) => {
      selectegAgents = selectegAgents + agent.userName + ',';
    })


    this.groupService.assignAgentsToGroup(this.data.groupId, selectegAgents).then((result) => {
      this.dialogRef.close(true);
    });
  }
}

export class AddMemberI {
  agents: Array<TmsAgentI>;
  groupId: number;
}
