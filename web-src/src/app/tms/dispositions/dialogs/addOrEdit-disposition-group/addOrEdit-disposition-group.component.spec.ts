import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddOrEditDispositionGroupComponent } from './addOrEdit-disposition-group.component';

describe('AddMemberComponent', () => {
  
  let component: AddOrEditDispositionGroupComponent;
  let fixture: ComponentFixture<AddOrEditDispositionGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddOrEditDispositionGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddOrEditDispositionGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
