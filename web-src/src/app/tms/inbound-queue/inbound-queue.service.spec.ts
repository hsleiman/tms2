import { TestBed, inject } from '@angular/core/testing';

import { InboundQueueService } from './inbound-queue.service';

describe('InboudQueueService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [InboundQueueService]
    });
  });

  it('should be created', inject([InboundQueueService], (service: InboundQueueService) => {
    expect(service).toBeTruthy();
  }));
});
