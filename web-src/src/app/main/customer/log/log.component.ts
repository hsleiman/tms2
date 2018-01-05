import { Component, OnInit, ViewChild} from '@angular/core';
import { MatTableDataSource, MatSort } from '@angular/material';
import { CustomerService, Log } from '../../customer/customer.service';

@Component({
  selector: 'app-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.css'],
  providers: [CustomerService]
})
export class LogComponent implements OnInit {

  @ViewChild(MatSort) sort: MatSort;

  logsDataSource: MatTableDataSource<Log> | null;
  logsTableColumnNames = ["flagged", "creationDate", "logType", "employeeId", "notes"];

  constructor(private mainService: CustomerService) { }

  ngOnInit() {
    let data = this.mainService.getLogsForCustomer();
    this.logsDataSource = new MatTableDataSource<Log>(data);
    this.logsDataSource.sort = this.sort;
  }

}