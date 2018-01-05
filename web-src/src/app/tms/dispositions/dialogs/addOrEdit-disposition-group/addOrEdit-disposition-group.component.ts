import { Component, OnInit, ViewEncapsulation, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatAutocompleteSelectedEvent } from '@angular/material';
import { TmsAgentI, AgentsService } from '../../../agents/agents.service';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';
import Disposition from '../../../dispositions/Disposition';
import DispositionGroup from '../../../dispositions/DispositionGroup';

@Component({
  selector: 'addOrEdit-disposition-group',
  templateUrl: './addOrEdit-disposition-group.component.html',
  styleUrls: ['./addOrEdit-disposition-group.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AddOrEditDispositionGroupComponent implements OnInit {
  public dispositionGroup: DispositionGroup = new DispositionGroup();
  public title: string = "Add Disposition Group";
  public form: FormGroup;
  
  constructor(public dialogRef: MatDialogRef<AddOrEditDispositionGroupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DispositionGroup, private _fb: FormBuilder) { 
      if(data) {
        this.title = "Edit Disposition Group";
        this.dispositionGroup = data;
      }

  }

  ngOnInit() {
    this.form = this._fb.group({
        name: ['', [<any>Validators.required]],
        description: ['', []]
    });
  }

  cancel() {
    this.dialogRef.close();
  }
  
  save() {
    if(!this.form.valid) {
      for (var i in this.form.controls) {
        this.form.controls[i].markAsTouched();
      }

      return;
    }

    this.dialogRef.close(this.dispositionGroup);
  }

}

