import { Component, OnInit, ViewEncapsulation, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatAutocompleteSelectedEvent } from '@angular/material';
import { TmsAgentI, AgentsService } from '../../../agents/agents.service';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';
import Disposition from '../../../dispositions/Disposition';
import { FormControl, FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'addOrEdit-disposition',
  templateUrl: './addOrEdit-disposition.component.html',
  styleUrls: ['./addOrEdit-disposition.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class AddOrEditDispositionComponent implements OnInit {
  public disposition: Disposition = new Disposition();
  public title: string = "Add Disposition";
  public form: FormGroup;
  public isShowMinuteField = false;
  public isShowRetriesField = false;
  public isShowSelect = false;
  
  constructor(public dialogRef: MatDialogRef<AddOrEditDispositionComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Disposition, private _fb: FormBuilder) { 
      if(data) {
        this.title = "Edit Disposition";
        this.disposition = data;
      }

      if(this.disposition.action == null) {
        // this.disposition.action = {
        //   actionType: null
        // }
      }
  }

  ngOnInit() {
    this.form = this._fb.group({
        name: ['', [<any>Validators.required]],
        code: ['', [<any>Validators.required]],
        status: ['', []],
        logType: ['',[]],
        action: ['', []],
        abandon: ['', []],
        contact: ['', []],
        followUp: ['', []],
        callBack: ['', []],
        success: ['', []],
        refusal: ['', []],
        exclusion: ['', []],
        rfdRequired: ['', []],
        ptpRequired: ['', []]
        // minutes: ['', []],
        // retries: ['', []],
        // select: ['', []]
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

    this.dialogRef.close(this.disposition);
  }

  toggleField() {
    if(this.disposition.action) {
      if(this.disposition.action.actionType == "DO_NOT_CALL" || this.disposition.action.actionType == "DO_NOT_CALL_NUMBER") {
        this.isShowSelect = true;
        this.isShowMinuteField = false;
        this.isShowRetriesField = false;
        return;
      }
      else if (this.disposition.action.actionType == "RETRY_CALL") {
        this.isShowMinuteField = true;
        this.isShowRetriesField = true;
        this.isShowSelect = false;
        return;
      }
    }

    this.isShowMinuteField = false;
    this.isShowRetriesField = false;
    this.isShowSelect = false;
  }

}

