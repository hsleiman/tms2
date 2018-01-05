import { Component, OnInit } from '@angular/core';


import { CallHistoryService, CallI } from './call-history.service';
import { MatTableDataSource, MatDialog, MatDialogConfig } from '@angular/material';


@Component({
  selector: 'app-call-history',
  templateUrl: './call-history.component.html',
  styleUrls: ['./call-history.component.scss'],
  providers: [CallHistoryService]
})

export class CallHistoryComponent implements OnInit {

  constructor(private callHistoryService: CallHistoryService, public dialog: MatDialog) { }
  public calls: Array<CallI>;
  dataSource: any;
  displayedColumns = ['Date', 'Number', 'Duration'];

  ngOnInit() {
    this.callHistoryService.getAgents().then((callHistory: Array<CallI>) => {
      this.calls = callHistory;
      this.dataSource = new MatTableDataSource(this.calls);
    }); 
  }

}
