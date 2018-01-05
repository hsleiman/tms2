import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { InboundQueuesService } from '../..//inbound-queues.service';

@Component({
  selector: 'app-add',
  templateUrl: './add-queue.component.html',
  styleUrls: ['./add-queue.component.css'],
  providers: [InboundQueuesService]
})
export class AddInboundQueueComponent implements OnInit {
  public data:any = {};
  constructor(public dialogRef: MatDialogRef<AddInboundQueueComponent>, private inboundQueuesService: InboundQueuesService) { }

  ngOnInit() {
  }

  addQueue() {
    this.data.dialerQueueType = 'INBOUND';
    this.inboundQueuesService.createQueue(this.data).then((result) => {
      this.dialogRef.close();
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
