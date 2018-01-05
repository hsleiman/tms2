import { Component } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { GroupI } from '../../../group/group.service';
import { GroupsService } from '../../groups.service';

@Component({
  selector: 'app-add',
  templateUrl: './add.component.html',
  styleUrls: ['./add.component.css'],
  providers: [ GroupsService ]
})
export class AddComponent  {

  public data = new GroupI();

  constructor(public dialogRef: MatDialogRef<AddComponent>, private groupsService: GroupsService) { }

  addGroup() {
    this.groupsService.addGroup(this.data).then((result) => {
      console.log(result);
      this.dialogRef.close(result);
    });
  }

  onNoClick() {
    this.dialogRef.close();
  }

}
