import { TestBed, inject } from '@angular/core/testing';

import { OutboundQueuesService } from './outbound-queues.service';

describe('OutboundQueuesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OutboundQueuesService]
    });
  });

  it('should be created', inject([OutboundQueuesService], (service: OutboundQueuesService) => {
    expect(service).toBeTruthy();
  }));
});
