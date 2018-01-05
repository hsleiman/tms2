import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OutboundQueuesComponent } from './outbound-queues.component';

describe('OutboundQueuesComponent', () => {
  let component: OutboundQueuesComponent;
  let fixture: ComponentFixture<OutboundQueuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OutboundQueuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OutboundQueuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
