import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AddInboundQueueComponent } from './add-queue.component';

describe('AddComponent', () => {
  let component: AddInboundQueueComponent;
  let fixture: ComponentFixture<AddInboundQueueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AddInboundQueueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AddInboundQueueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
