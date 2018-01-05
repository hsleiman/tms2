import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSort, MatTableDataSource, MatSnackBar } from '@angular/material';
import { AgentsService, AgentI, UpdateAgentSecurityVM, UpdateAgentInfoVM } from '../agents.service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import * as _ from 'lodash';

@Component({
  selector: 'app-agent-details',
  templateUrl: './agent-details.component.html',
  styleUrls: ['./agent-details.component.css'],
  providers: [AgentsService]
})
export class AgentDetailsComponent implements OnInit {

  allRoles: Array<any>;
  allPermissions: Array<any>;

  agentData: AgentI;

  agentRoles: Array<any>;
  agentPermissions: Array<any>;

  agentPersonalInfoForm: FormGroup;

  @ViewChild("roleTable", { read: MatSort }) roleSort: MatSort;
  @ViewChild("permTable", { read: MatSort }) permSort: MatSort;

  roleDataSource: MatTableDataSource<any> | null;
  roleTableColumnNames = ["id", "name", "assigned"];

  permDataSource: MatTableDataSource<any> | null;
  permTableColumnNames = ["id", "name", "assigned"];
  public isActive = false;


  constructor(private snackbar: MatSnackBar, private ar: ActivatedRoute, private agentsService: AgentsService) {
    this.init();
  }

  init(): void {
    this.agentData = new AgentI();
    this.agentPersonalInfoForm = new FormGroup({
      firstName: new FormControl(null, [Validators.required]),
      lastName: new FormControl(null, [Validators.required]),
      phoneNumber: new FormControl(null, [Validators.required]),
      phoneExtension: new FormControl(null, [Validators.required]),
      emailAddress: new FormControl(null, [Validators.required])
    });

    this.agentsService.getAllRoles().then(data => {
      this.allRoles = data;
      this.roleDataSource = new MatTableDataSource(data);
      this.roleDataSource.sort = this.roleSort;
    });

    this.agentsService.getAllPermissions().then(data => {
      this.allPermissions = data;
      this.permDataSource = new MatTableDataSource(data);
      this.permDataSource.sort = this.permSort;
    });

    this.getAgentInfo();
  
  }

  ngOnInit(): void {}

  getAgentInfo(): void {
    this.agentsService.getAgent(this.ar.snapshot.params["id"]).then(data => {
      this.agentData = data;
      console.log(data);
      this.agentPersonalInfoForm.controls["firstName"].setValue(this.agentData.firstName);
      this.agentPersonalInfoForm.controls["lastName"].setValue(this.agentData.lastName);
      this.agentPersonalInfoForm.controls["emailAddress"].setValue(this.agentData.emailAddress);
      this.agentPersonalInfoForm.controls["phoneNumber"].setValue(this.agentData.phoneNumber);
      this.agentPersonalInfoForm.controls["phoneExtension"].setValue(this.agentData.phoneExtension);
      //this.agentPersonalInfoForm.controls["isActive"].setValue(this.agentData.isActive);
      // this.isActive = this.agentData.isActive;

      this.getAgentRolesAndPermissions();
    });  
  }

  getAgentRolesAndPermissions(): void {
    let promises: Array<Promise<any>> = [];
    promises.push(this.agentsService.getAgentRoles(this.agentData.userName));
    promises.push(this.agentsService.getAgentPermissions(this.agentData.userName));
    Promise.all(promises).then(data => {
      this.agentRoles = data[0];
      this.agentPermissions = data[1];
      this.initializeAssignedRolesAndPermissions();
    });

    this.agentsService.getAgentRoles(this.agentData.userName).then(data => {
      this.agentRoles = data;
    });
    this.agentsService.getAgentPermissions(this.agentData.userName).then(data => {
      this.agentPermissions = data;
    });
  }

  initializeAssignedRolesAndPermissions(): void {
    for (let perm of this.permDataSource.data) {
      perm.assigned = _.find(this.agentPermissions, agentPerm => {
        return (agentPerm.id === perm.id);
      }) != null;
    }
    for (let role of this.roleDataSource.data) {
      role.assigned = _.find(this.agentRoles, agentRole => {
        return (agentRole.id === role.id);
      }) != null;
    }
  }

  applyFilter(filterValue: string, filterId: number): void {
    filterValue = filterValue.trim().toLowerCase();
    switch (filterId) {
      case 0:
        this.roleDataSource.filter = filterValue;
        break;
      case 1:
        this.permDataSource.filter = filterValue;
        break;
      default:
        break;
    }
  }

  resetChanges(): void {
    this.agentPersonalInfoForm.markAsPristine();
    this.getAgentInfo();
  }

  agentRolesOrPermissionsChanged(): boolean {
    let originalRoles = _.map(this.agentRoles, "id").sort((a, b) => { return a - b;});
    let originalPerms = _.map(this.agentPermissions, "id").sort((a, b) => { return a - b;});

    let newRoles = _.map(this.roleDataSource.data, role => {
      if (role.assigned === true) return role.id;
    });
    newRoles = _.without(newRoles, undefined).sort((a, b) => { return a - b;});
    let newPerms = _.map(this.permDataSource.data, perm => {
      if (perm.assigned === true) return perm.id;}).sort((a, b) => { return a - b;
    });
    newPerms = _.without(newPerms, undefined).sort((a, b) => { return a - b;});
    return !(_.isEqual(newRoles, originalRoles) && _.isEqual(newPerms, originalPerms));
  }

  saveRolesAndPerms(): Promise<any> {
    let newRolesAndPerms: UpdateAgentSecurityVM = new UpdateAgentSecurityVM();
    newRolesAndPerms.agentUsername = this.agentData.userName;
    newRolesAndPerms.roleIds = _.map(this.roleDataSource.data, role => {
      if (role.assigned === true) return role.id;
    });
    newRolesAndPerms.roleIds = _.without(newRolesAndPerms.roleIds, undefined);
    newRolesAndPerms.includePermissionIds = _.map(this.permDataSource.data, perm => {
      if (perm.assigned === true) return perm.id;
    });
    newRolesAndPerms.includePermissionIds = _.without(newRolesAndPerms.includePermissionIds, undefined);
    return this.agentsService.updateAgentSecurity(newRolesAndPerms);
  }

  savePersonalInfo(): Promise<any> {
    let newPersonalInfo = <UpdateAgentInfoVM>this.agentPersonalInfoForm.value;
    newPersonalInfo.userName = this.agentData.userName;
    newPersonalInfo.emailAddress = this.agentData.emailAddress;
    newPersonalInfo.phoneNumber = this.agentData.phoneNumber;
    newPersonalInfo.extension = this.agentData.phoneExtension;
    newPersonalInfo.status = 'INACTIVE';
    newPersonalInfo.voiceMailPassword = "testing";
    console.log(newPersonalInfo);
    return this.agentsService.updateAgentInfo(newPersonalInfo);
  }

  hasDataChanged(): boolean {
    if (this.agentData && this.permDataSource && this.roleDataSource) {
      return (this.agentRolesOrPermissionsChanged() || (this.agentPersonalInfoForm.dirty && this.agentPersonalInfoForm.valid));
    }
    return false;
  }

  saveChanges(): void {
    let rolesOrPermsChanged = this.agentRolesOrPermissionsChanged();
    let personalInfoChanged = this.agentPersonalInfoForm.dirty && this.agentPersonalInfoForm.valid;
    let promises: Array<Promise<any>> = [];
    if (rolesOrPermsChanged) promises.push(this.saveRolesAndPerms());
    if (personalInfoChanged) promises.push(this.savePersonalInfo());
    if (promises.length > 0) {
      Promise.all(promises).then(data => {
        this.snackbar.open("Changed user info successfully.", null, {duration: 3000});
        this.getAgentInfo();
      }, err => {
        //this.snackbar.open("Error changing user info.", null, {duration: 3000}); // Commented out; there's a false negative that happens because Angular has an issue with 200 status null responses for JSON content (i.e. ANY SUCCESS THAT DOESN'T RETURN ANYTHING SINCE ANGULAR DOES JSON BY DEFAULT), see their Github issue #19502. You would either have to set the content type to something other than JSON in the post request, or set up an interceptor in Spring to change the return status from 200 to 204 to bypass this. (Or have the method in REST not return null/void in this case.)
      });
    }
    else {
      this.snackbar.open("No changes made for user.", null, {duration: 3000})
    }
  }

}
