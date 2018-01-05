import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

import { InboundQueueQueueI } from '../../../inbound-queue/inbound-queue.service'

@Component({
  selector: 'app-add',
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.css']
})
export class AddComponent implements OnInit {
  public data = new InboundQueueQueueI();
  constructor(public dialogRef: MatDialogRef<AddComponent>, private outboundQueuesService) { }

  ngOnInit() {
  }

  addQueue() {
    this.data.dialerQueueType = 'INBOUND';
    this.outboundQueuesService.createQueue(this.data).then((result:InboundQueueQueueI) => {
      this.dialogRef.close();
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
