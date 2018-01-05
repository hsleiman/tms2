import { Component, OnInit } from '@angular/core';
import { OutboundQueuesService, OutboundQueueResult } from '../outbound-queues/outbound-queues.service';
import { OutboundQueueI } from '../outbound-queue/outbound-queue.service';

@Component({
  selector: 'app-campaigns',
  templateUrl: './campaigns.component.html',
  styleUrls: ['./campaigns.component.scss'],
  providers: [OutboundQueuesService]
})
export class CampaignsComponent implements OnInit {

public navs = [{name:'Campaigns', route:'/tms/campaigns'}];

  public queues: Array<OutboundQueueResult>;
  constructor(private outboundQueuesService: OutboundQueuesService) { }

  ngOnInit() {
    this.outboundQueuesService.getAllDialerQueues().then((result: Array<OutboundQueueResult>) => {
      this.queues = result;
    })
  }

}
