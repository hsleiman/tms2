import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PermissionRolesComponent } from './permission-roles.component';

describe('PermissionRolesComponent', () => {
  let component: PermissionRolesComponent;
  let fixture: ComponentFixture<PermissionRolesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PermissionRolesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PermissionRolesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
