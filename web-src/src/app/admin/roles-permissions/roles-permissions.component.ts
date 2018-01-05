import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSort, MatTableDataSource } from '@angular/material';
import { AgentsService } from '../agents/agents.service';
import PermissionsGroup from '../shared-modules/admin/models/PermissionsGroup';
import { Permission } from '../shared-modules/admin/enums/Permission';

@Component({
  selector: 'app-roles-permissions',
  templateUrl: './roles-permissions.component.html',
  styleUrls: ['./roles-permissions.component.css'],
  providers: [AgentsService]
})
export class RolesPermissionsComponent implements OnInit {

  public avbPerms: Array<any> = [];
  public assignedPerms: Array<any> = [];
  rolesDataSource: MatTableDataSource<any> | null;
  rolesTableColumnNames = ["id", "name", "description"];

  avalblPermsDataSource: MatTableDataSource<any> | null;
  avalblPermsTableColumnNames = ["id", "name", "description", "forwordArrow"];

  allPermissions: any;

  assignedPermsTableDataSource: MatTableDataSource<any> | null;
  assignedPermsTableColumnNames = ["backwardArrow", "id", "name", "description"];

  public permsInGroup: Permission[];
  public assignedPermsInGroup: PermissionsGroup[];

  selectedRolePermissions: PermissionsGroup[] = [];
  selectedRoleUnassignedPermissions: PermissionsGroup[] = [];
  permissions: PermissionsGroup[]

  @ViewChild("rolesTable", { read: MatSort }) rolePermSort: MatSort;
  @ViewChild("permRoleTable", { read: MatSort }) permRoleSort: MatSort;
  @ViewChild("assignedPermsTable", { read: MatSort }) permSort: MatSort;

  constructor(private agentsService: AgentsService) { }

  ngOnInit() {
    this.agentsService.getAllRoles().then(data => {
      this.rolesDataSource = new MatTableDataSource(data);
    });
  }

  loadAllPermissions(): void {
    this.agentsService.getAllPermissions().then(data => {
      this.avalblPermsDataSource = new MatTableDataSource<any>(data);
      this.allPermissions = new MatTableDataSource(data);
    });
  }

  applyFilterOnRolesTab(filterValue: string): void {
    filterValue = filterValue.trim().toLowerCase();
    this.rolesDataSource.filter = filterValue;
  }

  applyFilterOnAvablePerms(filterValue: string) {
    filterValue = filterValue.trim().toLowerCase();
    this.avalblPermsDataSource.filter = filterValue;
  }

  applyFilterOnAssignedPerms(filterValue: string) {
    filterValue = filterValue.trim().toLowerCase();
  }

  addPermissions(permission: PermissionsGroup) {

    let p = this.selectedRoleUnassignedPermissions.find((item: PermissionsGroup) => {
      return item.id == permission.id;
    });

    if (!p) {
      this.selectedRoleUnassignedPermissions.push(permission);
      this.assignedPermsTableDataSource = new MatTableDataSource(this.selectedRoleUnassignedPermissions);

    }

  }

  removePermissions(permission: PermissionsGroup) {
    let indx = this.selectedRoleUnassignedPermissions.indexOf(permission);

    if (indx > -1) {
      this.selectedRoleUnassignedPermissions.splice(indx, 1);
      this.assignedPermsTableDataSource = new MatTableDataSource(this.selectedRoleUnassignedPermissions);
    }
  }



}

