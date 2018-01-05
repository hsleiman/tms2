import { Component, ViewEncapsulation, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { GroupI } from '../../../group/group.service';
import { GroupsService } from '../../groups.service';

@Component({
  selector: 'app-edit',
  templateUrl: './edit.component.html',
  styleUrls: ['./edit.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [GroupsService]
})
export class EditComponent {

  constructor(public dialogRef: MatDialogRef<EditComponent>, private groupsService: GroupsService
    , @Inject(MAT_DIALOG_DATA) public data: GroupI) {
  }
  edit() {
    this.groupsService.addGroup(this.data).then((result) => {
      console.log(result);
      this.dialogRef.close(result);
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
