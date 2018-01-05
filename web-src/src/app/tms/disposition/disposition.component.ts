import { Component, OnInit, group } from '@angular/core';
import { DispositionService } from '../dispositions/dispositions.service';
import { MatTableDataSource, MatDialog } from '@angular/material';
import { AddOrEditDispositionComponent } from './dialogs/addOrEdit-disposition/addOrEdit-disposition.component';
import { Subscription } from 'rxjs/Subscription';
import Disposition from '../dispositions/Disposition';
import DispositionGroup from '../dispositions/DispositionGroup';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';

@Component({
  selector: 'app-disposition',
  templateUrl: './disposition.component.html',
  styleUrls: ['./disposition.component.scss'],
  providers: [DispositionService]
})
export class DispositionComponent implements OnInit {

  public dispositions: Array<Disposition> = [];
  public oldDispositions: Array<Disposition> = [];  //keep a copy of all disposition codes
  public dispositionGroupDataSource: any;
  public dispositionGroupDisplayedColumns = ['pk', 'name', "Actions"];
  public dispositionInGroupDataSource: any;
  public dispositionInGroupDisplayedColumns = ['disposition', "Actions"];
  public dispositionDataSource: any;
  public dispositionDisplayedColumns = ['disposition', 'Actions'];
  public dispositionsInGroup: Array<Disposition> = [];
  public dispositionGroupPk: number;
  public subscription: Subscription;
  public navParams;

  constructor(private route: ActivatedRoute, private router: Router, 
              public dispositionService: DispositionService, public dialog: MatDialog) {
    this.getAllCallDispositionCodes();

    //reload table
    this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);
    this.dispositionDataSource = new MatTableDataSource(this.dispositions);
  }

  ngOnInit() {
    this.route.params.subscribe((param) => {
      this.dispositionGroupPk = param['id'];
      this.navParams = [{name:'Disposition', route:'/tms/dispositions'}, {name: param['id'], route: `/tms/disposition/${param['id']}`}];
    })

    this.getAllCallDispositionsForGroup(this.dispositionGroupPk);
  }


  getAllCallDispositionsForGroup(dispositionGroupPk: number) {
    this.dispositions = Object.assign([], this.oldDispositions);

    this.dispositionService.getAllCallDispositionsForGroup(dispositionGroupPk).then((dispositions: Array<Disposition>) => {
      this.dispositionsInGroup = dispositions;

      this.filterDispositions();

      //reload table
      this.dispositionDataSource = new MatTableDataSource(this.dispositions);
      this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);
    });
  }

  filterDispositions() {
    this.dispositionsInGroup.forEach((d: Disposition) => {
      let index = this.dispositions.findIndex((value: Disposition) => {
        return value.dispositionId == d.dispositionId;
      })

      if (index > -1) {
        this.dispositions.splice(index, 1);
      }
    })
  }

  addOrEditDisposition(disposition: Disposition) {
    let dialogRef = null;

    if (disposition != null) {
      dialogRef = this.dialog.open(AddOrEditDispositionComponent, {
        width: '500px',
        data: Object.assign({}, disposition)
      });
    } else {
      dialogRef = this.dialog.open(AddOrEditDispositionComponent, {
        width: '500px',
        data: ''
      });
    }

    this.subscription = dialogRef.afterClosed().subscribe((result: Disposition) => {
      if (result) {
        if (result.dispositionId) {
          //update disposition
          this.dispositionService.addOrUpdateCallDisposition(result).then((data) => {
            this.getAllCallDispositionCodes();
            this.getAllCallDispositionsForGroup(this.dispositionGroupPk);
          })
        } else {
          //new disposition
          result.dispositionId = 0; //Otherwise null pointer exception from BE
          this.dispositionService.addOrUpdateCallDisposition(result).then((data) => {
            this.getAllCallDispositionCodes();
            this.getAllCallDispositionsForGroup(this.dispositionGroupPk);
          })
        }
      }
    });
  }

  addDispositionInDispositionGroup(dispostion: Disposition) {

    this.dispositionsInGroup.push(dispostion);

    this.addCallDispositionCodesToGroup();

    let index = this.dispositions.indexOf(dispostion);
    this.dispositions.splice(index, 1);

    //reload table
    this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);
    this.dispositionDataSource = new MatTableDataSource(this.dispositions);
  }

  getAllCallDispositionCodes() {
    this.dispositionService.getAllCallDispositionCodes().then((dispostions: Array<Disposition>) => {
      this.dispositions = dispostions;
      this.oldDispositions = dispostions;
      this.filterDispositions();
      this.dispositionDataSource = new MatTableDataSource(this.dispositions);
    })
  }

  removeDispositionFromDispositionGroup(dispostion: Disposition) {
    this.dispositions.push(dispostion);

    let index = this.dispositionsInGroup.indexOf(dispostion);
    this.dispositionsInGroup.splice(index, 1);

    this.addCallDispositionCodesToGroup();

    //reload table
    this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);
    this.dispositionDataSource = new MatTableDataSource(this.dispositions);
  }

  addCallDispositionCodesToGroup() {
    let ids: Array<number> = [];

    this.dispositionsInGroup.forEach((disposition: Disposition) => {
      ids.push(disposition.dispositionId);
    })

    this.dispositionService.addCallDispositionCodesToGroup(this.dispositionGroupPk, ids).then((data) => {
      console.log(data);
    });
  }

  dispositionGroupFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dispositionGroupDataSource.filter = filterValue;
  }

  dispositionFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dispositionDataSource.filter = filterValue;
  }

  dispositionInGroupFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // MatTableDataSource defaults to lowercase matches
    this.dispositionInGroupDataSource.filter = filterValue;
  }

  ngOnDestroy() {
    if(this.subscription)
      this.subscription.unsubscribe();
  }
}
