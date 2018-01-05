import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { OutboundQueueI } from '../../../outbound-queue/outbound-queue.service';
import { OutboundQueuesService } from '../../outbound-queues.service';

@Component({
  selector: 'app-add-queue',
  templateUrl: './add-queue.component.html',
  styleUrls: ['./add-queue.component.css'], providers: [OutboundQueuesService]
})
export class AddQueueComponent implements OnInit {
  public data = new OutboundQueueI();
  constructor(public dialogRef: MatDialogRef<AddQueueComponent>, private outboundQueuesService: OutboundQueuesService) { }

  ngOnInit() {
  }

  addQueue() {
    this.data.dialerQueueType = 'OUTBOUND';
    this.outboundQueuesService.createQueue(this.data).then((result) => {
      this.dialogRef.close();
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
