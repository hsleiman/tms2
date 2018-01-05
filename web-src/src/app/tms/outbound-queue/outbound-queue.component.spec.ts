import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OutboundQueueComponent } from './outbound-queue.component';

describe('OutboundQueueComponent', () => {
  let component: OutboundQueueComponent;
  let fixture: ComponentFixture<OutboundQueueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OutboundQueueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OutboundQueueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
