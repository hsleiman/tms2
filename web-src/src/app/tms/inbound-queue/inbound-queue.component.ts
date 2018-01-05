import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { InboundQueuesService } from '../inbound-queues/inbound-queues.service';
import InboundQueue from '../inbound-queues/InboundQueue';
import { GroupsService } from '../groups/groups.service';
import { GroupI } from '../group/group.service';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { MatTableDataSource, MatDialog } from '@angular/material';
import 'rxjs/add/operator/switchMap';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';
import DispositionGroup from '../dispositions/DispositionGroup';
import InboundDialerQueueSettings from "../inbound-queues/InboundDialerQueueSettings";
import { DispositionService } from '../dispositions/dispositions.service';
import { CallAgentType } from '../enums/CallAgentType';

@Component({
  selector: 'app-inbound-queue',
  templateUrl: './inbound-queue.component.html',
  styleUrls: ['./inbound-queue.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [InboundQueuesService, GroupsService, DispositionService]
})
export class InboundQueueComponent implements OnInit {

  private groups: Array<GroupI>;
  private queue: InboundQueue = new InboundQueue();
  public errorNotFound = false;
  public phoneList = ['Home Phone 1', 'Home Phone 2', 'Work Phone', 'Mobile Phone'];
  public OneToTen = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  public displayModes = [
    {key: "NEW_WINDOW", value: 'New Window'},
    {key: "NEW_TAB", value: "New Tab"},
    {key: "SAME_WINDOW", value: "Same Window"}
  ];
  public callOptions = [
    {key: "LONGEST_IDLE", value: 'Longest Idle'},
    {key: "SHORTEST_IDLE", value: 'Shortest Idle'},
    {key: "SKILL_BASED", value: 'Skill Based'},
    {key: "ROUND_ROBIN", value: 'Round Robin'},
    {key: "ROUND_ROBIN_UTILIZATION", value: 'Round Robin Utilization'}
  ]
  public form: FormGroup;
  public dispositionGroups: Array<DispositionGroup>;
  public dqSettings: InboundDialerQueueSettings = new InboundDialerQueueSettings();
  public primaryAgentIsInline: boolean;
  public queueGroupAgentIsInline: Boolean;
  public secondaryAgentIsInline: Boolean;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private inboundQueuesService: InboundQueuesService,
    private groupsService: GroupsService,
    private _fb: FormBuilder,
    private dispositionService: DispositionService) { 

  }

  ngOnInit() {
    this.groupsService.getDialerGroups().then((result) => {
      this.groups = result;
    });

    let id = 0;

    this.route.params.subscribe((param) => {
      id = param['id'];
    })
    
    this.getAllDispositionGroups();
    this.getInboundDQSettingsByDQPk(id);

    this.form = this._fb.group({
      dispositionGroup: ['', []],
      queuePriority: ['', []],
      queueWeight: ['', []],
      wrapTime: ['', []],
      idleTime: ['', []],
      callRoutingOption: ['', []],
      popupDisplayMode: ['', []],
      startTime: ['', []],
      endTime: ['', []],
      autoAnswerEnabled: ['',[]],
      primaryAgentIsInline: ['', []],
      queueGroupAgentIsInline: ['', []],
      secondaryAgentIsInline: ['', []]
    });
  }

  getAllDispositionGroups() {
    this.dispositionService.getAllDispositionGroups().then((data:Array<DispositionGroup>) => {
      this.dispositionGroups = data;
    })
  }

  getInboundDQSettingsByDQPk(pk: number) {
    this.inboundQueuesService.getInboundDQSettingsByDQPk(pk).then((data: InboundDialerQueueSettings) => {
      if(data && data.agentCallOrder) {
        data.agentCallOrder.forEach((agent) => {
          if(agent.incomingCallAgent == CallAgentType.Primary)
            this.primaryAgentIsInline = agent.inline;

          if(agent.incomingCallAgent == CallAgentType.Queue_Group)
            this.queueGroupAgentIsInline = agent.inline;

          if(agent.incomingCallAgent == CallAgentType.Secondary)
            this.secondaryAgentIsInline = agent.inline;
        })
      }
      
      this.dqSettings = data;
    })
  }

  updateInboundDQSettings() {
    if(this.dqSettings) {
      this.dqSettings.agentCallOrder = [];
      this.dqSettings.agentCallOrder.push({
            incomingCallAgent : CallAgentType.Primary,
            inline: this.primaryAgentIsInline
      });

      this.dqSettings.agentCallOrder.push({
            incomingCallAgent : CallAgentType.Queue_Group,
            inline: this.queueGroupAgentIsInline
      })

      this.dqSettings.agentCallOrder.push({
            incomingCallAgent : CallAgentType.Secondary,
            inline: this.secondaryAgentIsInline
      })
    }
        
    this.inboundQueuesService.updateInboundDQSettings(this.dqSettings).then((data: InboundDialerQueueSettings) => {
      this.dqSettings = data;
    })
  }
}
