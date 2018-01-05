import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { InboundQueuesService } from './inbound-queues.service';
import { MatTableDataSource, MatDialog } from '@angular/material';
import { EditInboundQueueComponent } from './dialogs/edit/edit-inbound-queue.component';
import { Subscription } from 'rxjs/Subscription';
import { DialerQueueType} from '../enums/DailerQueueType';
import InboundQueue from './InboundQueue';
import { AddInboundQueueComponent } from './dialogs/add/add-queue.component';

@Component({
  selector: 'app-inboud-queues',
  templateUrl: './inbound-queues.component.html',
  styleUrls: ['./inbound-queues.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [InboundQueuesService]
})
export class InboundQueuesComponent implements OnInit {

  public queues: Array<InboundQueue>;
  public dataSource: any;
  public displayedColumns = ['pk', 'groupName', 'queueName', 'Loans', 'Edit', 'Delete'];
  public subscription: Subscription;
  
  constructor(public inboudQueuesService: InboundQueuesService, public dialog: MatDialog,  private matDialog: MatDialog) {
    
  }

  ngOnInit() {
    this.getAllInboundDialerQueues();
  }

  ngOnDestroy() {
    if(this.subscription != null)
      this.subscription.unsubscribe();
  }

  getAllInboundDialerQueues() {
    this.inboudQueuesService.getAllDialerQueues(DialerQueueType.Inbound).then((queues: Array<InboundQueue>) => {
      this.queues = queues;
      this.dataSource = new MatTableDataSource(this.queues);
    })
  }

  addQueue() {
    this.matDialog.open(AddInboundQueueComponent).afterClosed().subscribe(() => {
      this.getAllInboundDialerQueues();
    });
  }

  editInboundQueue(data: InboundQueue) {
    const dialogRef = this.dialog.open(EditInboundQueueComponent, {
      width: '350px',
      data: Object.assign({}, data)
    });

    this.subscription = dialogRef.afterClosed().subscribe(result => {
      if(result) {
        this.getAllInboundDialerQueues();
      }
    });
  }

  deleteQueue(queue: InboundQueue) {
    this.inboudQueuesService.DeleteQueue(queue.dialerQueueDetails.pk).then((data) => {
      this.getAllInboundDialerQueues();
    })
  }
}
