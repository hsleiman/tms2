import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddOrEditDispositionComponent } from './addOrEdit-disposition.component';

describe('AddMemberComponent', () => {
  let component: AddOrEditDispositionComponent;
  let fixture: ComponentFixture<AddOrEditDispositionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddOrEditDispositionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddOrEditDispositionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
