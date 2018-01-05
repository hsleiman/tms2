import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { OutboundQueuesService, OutboundQueueResult, DialerStatusI } from './outbound-queues.service';
import { OutboundQueueI } from '../outbound-queue/outbound-queue.service';
import { MatTableDataSource, MatDialog } from '@angular/material';
import { AddQueueComponent } from './dialogs/add-queue/add-queue.component';
import { EditQueueComponent } from './dialogs/edit-queue/edit-queue.component';

@Component({
  selector: 'app-outbound-queues',
  templateUrl: './outbound-queues.component.html',
  styleUrls: ['./outbound-queues.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [OutboundQueuesService]
})
export class OutboundQueuesComponent implements OnInit {

  constructor(private outboundQueuesService: OutboundQueuesService, private matDialog: MatDialog) { }
  public queues: any;
  dataSource: any;
  displayedColumns = ['pk', 'queueName', 'groupName', 'loanCount', 'Edit', 'Delete'];
  ngOnInit() {
    this.outboundQueuesService.getAllDialerQueues().then((result: Array<OutboundQueueResult>) => {
      this.queues = result;
      console.log(this.queues);
      this.dataSource = new MatTableDataSource(this.queues);
    });
  }

  addQueue() {
    this.matDialog.open(AddQueueComponent).afterClosed().subscribe(() => {

    });
  }
  editQueue(queue) {
    this.matDialog.open(EditQueueComponent, {
      width: '250px',
      data: { queue: queue }
    }).afterClosed().subscribe(() => {

    });

  }
}
