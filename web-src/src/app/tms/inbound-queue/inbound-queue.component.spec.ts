import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InboundQueueComponent } from './inbound-queue.component';

describe('InboundQueueComponent', () => {
  let component: InboundQueueComponent;
  let fixture: ComponentFixture<InboundQueueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InboundQueueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InboundQueueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
