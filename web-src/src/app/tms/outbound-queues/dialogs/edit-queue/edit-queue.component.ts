import { Component, OnInit, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatAutocompleteSelectedEvent } from '@angular/material';
import { OutboundQueueI } from '../../../outbound-queue/outbound-queue.service';
import { OutboundQueuesService } from '../../outbound-queues.service';
import { log } from 'util';

@Component({
  selector: 'app-edit-queue',
  templateUrl: './edit-queue.component.html',
  styleUrls: ['./edit-queue.component.scss']
})
export class EditQueueComponent implements OnInit {
  public info:OutboundQueueI;
  constructor( @Inject(MAT_DIALOG_DATA) public data:any) { }

  ngOnInit() {
    this.info = this.data.queue.dialerQueueDetails;
  }

  closeDialog(){
    
  }

  updateQueue(){
    
  }

}
