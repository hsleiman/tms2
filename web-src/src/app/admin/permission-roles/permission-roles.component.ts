import { Component, OnInit, ViewChild } from '@angular/core';
import { MatSort, MatTableDataSource } from '@angular/material';
import { AgentsService } from '../agents/agents.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-permission-roles',
  templateUrl: './permission-roles.component.html',
  styleUrls: ['./permission-roles.component.css'],
  providers: [AgentsService, Title]
})
export class PermissionRolesComponent implements OnInit {

  @ViewChild("permTable", { read: MatSort }) permSort: MatSort;
  @ViewChild("permRoleTable", { read: MatSort }) permRoleSort: MatSort;

  permDataSource: MatTableDataSource<any> | null;
  permTableColumnNames = ["id", "name", "description"];

  permRoleDataSource: MatTableDataSource<any> | null;
  permRoleTableColumnNames = ["id", "name", "description"];

  selectedPerm: any;

  constructor(private titleService: Title, private agentsService: AgentsService) { }

  ngOnInit() {
    this.titleService.setTitle("SettleIt - Permission's Roles");
    this.agentsService.getAllPermissions().then(data => {
      this.permDataSource = new MatTableDataSource<any>(data);
      this.permDataSource.sort = this.permSort;
    });
  }

  applyRoleFilter(filterValue: string): void {
    filterValue = filterValue.trim().toLowerCase();
    this.permDataSource.filter = filterValue;
  }

  applyRolePermFilter(filterValue: string): void {
    filterValue = filterValue.trim().toLowerCase();
    this.permRoleDataSource.filter = filterValue;
  }

  loadPermRoles(permission: any): void {
    if (this.selectedPerm == null || (this.selectedPerm.id !== permission.id)) {
      this.selectedPerm = permission;
      this.agentsService.getPermissionRoles(this.selectedPerm.id).then(data => {
        this.permRoleDataSource = new MatTableDataSource<any>(data);
        this.permRoleDataSource.sort = this.permRoleSort;
      });
    }
  }

  toggleSelectedPerm(permission: any): string {
    if (this.selectedPerm != null && (permission.id === this.selectedPerm.id)) {
      return "perm-selected";
    }
    return "";
  }

}
