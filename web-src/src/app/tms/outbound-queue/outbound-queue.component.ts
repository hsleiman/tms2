import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { OutboundQueueService } from './outbound-queue.service';
import { OutboundQueueI } from './outbound-queue.service';
import { GroupsService } from '../groups/groups.service';
import { GroupI } from '../group/group.service';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { MatTableDataSource, MatDialog } from '@angular/material';
import 'rxjs/add/operator/switchMap';
import DispositionGroup from '../dispositions/DispositionGroup';
import OutboundDialerQueueSettings from '../outbound-queues/OutboundDialerQueueSettings';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { DispositionService } from '../dispositions/dispositions.service';
import { CallAgentType } from '../enums/CallAgentType';

@Component({
  selector: 'app-outbound-queue',
  templateUrl: './outbound-queue.component.html',
  styleUrls: ['./outbound-queue.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [OutboundQueueService, GroupsService, DispositionService]
})
export class OutboundQueueComponent implements OnInit {

  private groups: Array<GroupI>;
  private queue: OutboundQueueI = new OutboundQueueI();
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
  public dqSettings: OutboundDialerQueueSettings = new OutboundDialerQueueSettings();
  // public primaryAgentIsInline: boolean;
  // public queueGroupAgentIsInline: Boolean;
  // public secondaryAgentIsInline: Boolean;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private outboundQueueService: OutboundQueueService,
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
    this.getOutboundDQSettingsByDQPk(id);

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

  getOutboundDQSettingsByDQPk(pk: number) {
    this.outboundQueueService.getOutboundDQSettingsByDQPk(pk).then((data: OutboundDialerQueueSettings) => {
      
      this.dqSettings = data;
    })
  }

  updateInboundDQSettings() {
    this.outboundQueueService.updateOutboundDQSettings(this.dqSettings).then((data: OutboundDialerQueueSettings) => {
      this.dqSettings = data;
    })
  }

}
