import { Component, OnInit, group } from '@angular/core';
import { DispositionService } from './dispositions.service';
import { MatTableDataSource, MatDialog } from '@angular/material';
import { AddOrEditDispositionGroupComponent } from './dialogs/addOrEdit-disposition-group/addOrEdit-disposition-group.component';
import { AddOrEditDispositionComponent } from '../disposition/dialogs/addOrEdit-disposition/addOrEdit-disposition.component';
import { Subscription } from 'rxjs/Subscription';
import Disposition from './Disposition';
import DispositionGroup from './DispositionGroup';

@Component({
  selector: 'app-dispositions',
  templateUrl: './dispositions.component.html',
  styleUrls: ['./dispositions.component.scss'],
  providers: [DispositionService]
})
export class DispositionsComponent implements OnInit {

  public dispositionGroups: Array<DispositionGroup> = [];
  public dispositions: Array<Disposition> = [];
  public oldDispositions: Array<Disposition> = [];  //keep a copy of all disposition codes
  public dispositionGroupDataSource: any;
  public dispositionGroupDisplayedColumns = ['pk', 'name', "Actions"];
  public dispositionInGroupDataSource: any;
  public dispositionInGroupDisplayedColumns = ['disposition', "Actions", "forwordArrow"];
  public dispositionDataSource: any;
  public dispositionDisplayedColumns = ['backwardArrow', 'disposition', "Actions"];
  public dispositionsInGroup: Array<Disposition> = [];
  public showPanel = false;
  public isFirstTimeOpenAllPanels = true;
  public showLastTwoPanels = false;
  public selectedDispositionGroup: DispositionGroup;

  public subscription: Subscription;
  public navParams = [{name:'Dispositions', route:'/tms/dispositions'}];
  
  constructor(public dispositionService: DispositionService, public dialog: MatDialog) {
    this.getDispositionGroups();
    this.getAllCallDispositionCodes();

    //reload table
    this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);
    this.dispositionDataSource = new MatTableDataSource(this.dispositions);
  }

  ngOnInit() {
  }

  getDispositionGroups() {
    this.dispositionService.getAllDispositionGroups().then((groups: Array<DispositionGroup>) => {
      this.dispositionGroups = groups;
      this.dispositionGroupDataSource = new MatTableDataSource(this.dispositionGroups);
    })
  }

  getAllCallDispositionsForGroup(dispositionGroup: DispositionGroup) {

    this.selectedDispositionGroup = dispositionGroup;
    this.dispositions = Object.assign([], this.oldDispositions);

    this.dispositionService.getAllCallDispositionsForGroup(dispositionGroup.pk).then((dispositions: Array<Disposition>) => {
      this.dispositionsInGroup = dispositions;

      this.filterDispositions();

      //reload table
      this.dispositionDataSource = new MatTableDataSource(this.dispositions);
      this.dispositionInGroupDataSource = new MatTableDataSource(this.dispositionsInGroup);

      this.showLastTwoPanels = true;
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

  addOrEditDispositionGroup(dispositionGroup: DispositionGroup) {
    let dialogRef = null;

    if (dispositionGroup != null) {
      dialogRef = this.dialog.open(AddOrEditDispositionGroupComponent, {
        width: '500px',
        data: Object.assign({}, dispositionGroup)
      });
    } else {
      dialogRef = this.dialog.open(AddOrEditDispositionGroupComponent, {
        width: '500px',
        data: ''
      });
    }

    this.subscription = dialogRef.afterClosed().subscribe((result: DispositionGroup) => {
      if (result) {
        if (result.pk) {
          //update group
          this.dispositionService.addOrUpdateCallDispositionGroup(result).then((data) => {
            this.getDispositionGroups();
          })
        } else {
          //new group
          this.dispositionService.addOrUpdateCallDispositionGroup(result).then((data) => {
            this.getDispositionGroups();
          });
        }
      }
    });
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
            this.getAllCallDispositionsForGroup(this.selectedDispositionGroup);
          })
        } else {
          //new disposition
          result.dispositionId = 0; //Otherwise null pointer exception from BE
          this.dispositionService.addOrUpdateCallDisposition(result).then((data) => {
            this.getAllCallDispositionCodes();
            this.getAllCallDispositionsForGroup(this.selectedDispositionGroup);
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

    this.dispositionService.addCallDispositionCodesToGroup(this.selectedDispositionGroup.pk, ids).then((data) => {
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

  openPanel(dispositionGroup: DispositionGroup) {
    this.showPanel = true;
    this.showLastTwoPanels = false;
    this.isFirstTimeOpenAllPanels = false;
    this.getAllCallDispositionsForGroup(dispositionGroup);
    return false;
  }

  ngOnDestroy() {
    if(this.subscription)
      this.subscription.unsubscribe();
  }
}
