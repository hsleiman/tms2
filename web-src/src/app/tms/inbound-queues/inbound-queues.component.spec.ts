import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InboundQueuesComponent } from './inbound-queues.component';

describe('InboudQueuesComponent', () => {
  let component: InboundQueuesComponent;
  let fixture: ComponentFixture<InboundQueuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InboundQueuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InboundQueuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
