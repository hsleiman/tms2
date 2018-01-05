import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditInboundQueueComponent } from './edit-inbound-queue.component';

describe('EditComponent', () => {
  let component: EditInboundQueueComponent;
  let fixture: ComponentFixture<EditInboundQueueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditInboundQueueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditInboundQueueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
