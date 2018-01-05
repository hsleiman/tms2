import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { GroupsService } from './groups.service';
import { GroupI } from '../group/group.service';
import { EditComponent } from './dialogs/edit/edit.component';
import { AddComponent } from './dialogs/add/add.component';
import { MatTableDataSource, MatDialog } from '@angular/material';

@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [GroupsService]
})
export class GroupsComponent implements OnInit {

  constructor(private groupService: GroupsService, public dialog: MatDialog) { }
  public navParams = [{name:'Groups', route:'/tms/groups'}];
  public groups: Array<GroupI>;
  dataSource: any;
  displayedColumns = ['pk', 'groupName', 'comment', 'Edit'];
  ngOnInit() {
    this.groupService.getDialerGroups().then((result: Array<GroupI>) => {
      this.groups = result;
      this.dataSource = new MatTableDataSource(this.groups);
    });
  }

  editGroup(data) {
    const dialogRef = this.dialog.open(EditComponent, {
      width: '350px',
      data: data
    });

    dialogRef.afterClosed().subscribe(result => {
      this.ngOnInit();
    });
  }

  addGroup() {
    const dialogRef = this.dialog.open(AddComponent, {
      width: '350px'
    });

    dialogRef.afterClosed().subscribe(result => {
      this.ngOnInit();
    });
  }

}

export class DialerQueue {
  dialerQueueIOType: string;
  queryString: string;
  queueName: string;
}
