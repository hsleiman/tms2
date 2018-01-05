import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddEditEmailComponent } from './add-edit-email.component';

describe('AddEditEmailComponent', () => {
  let component: AddEditEmailComponent;
  let fixture: ComponentFixture<AddEditEmailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddEditEmailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddEditEmailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
