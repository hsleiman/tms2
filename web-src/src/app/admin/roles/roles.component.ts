import { Component, OnInit } from '@angular/core';
import { RolesService } from './roles.service';

@Component({
  selector: 'app-roles',
  templateUrl: './roles.component.html',
  styleUrls: ['./roles.component.css'],
  providers: [RolesService]
})
export class RolesComponent implements OnInit {

  constructor(private rolesService: RolesService) { }

  ngOnInit() {
    this.rolesService.getRoles().then((result) => {
      console.log(result);
    });
  }

}
