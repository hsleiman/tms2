import { TestBed, inject } from '@angular/core/testing';

import { InboundQueuesService } from './inbound-queues.service';

describe('InboundQueuesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InboundQueuesService]
    });
  });

  it('should be created', inject([InboundQueuesService], (service: InboundQueuesService) => {
    expect(service).toBeTruthy();
  }));
});
