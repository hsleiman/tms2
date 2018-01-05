import { Component, ViewEncapsulation, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { DispositionService } from '../../../dispositions/dispositions.service';
import { InboundQueuesService } from '../../inbound-queues.service';
import DispositionGroup from '../../../dispositions/DispositionGroup';
import InboundQueue from '../../../inbound-queues/InboundQueue';

@Component({
  selector: 'edit-inbound-queue',
  templateUrl: './edit-inbound-queue.component.html',
  styleUrls: ['./edit-inbound-queue.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [DispositionService, InboundQueuesService]
})
export class EditInboundQueueComponent {

  public form: FormGroup;
  public dispositionGroups: Array<DispositionGroup> = [];

  constructor(public dialogRef: MatDialogRef<EditInboundQueueComponent>,
    @Inject(MAT_DIALOG_DATA) public data: InboundQueue, private _fb: FormBuilder,
    private inboundQueuesService: InboundQueuesService) {

  }

  ngOnInit() {

  }

  save(): void {
    //call service
    this.inboundQueuesService.updateQueue(this.data.dialerQueueDetails).then((data) => {
      this.dialogRef.close(this.data);
    });
  }

  cancel(): void {
    if(this.dialogRef)
      this.dialogRef.close();
  }
}
