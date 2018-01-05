import { TestBed, inject } from '@angular/core/testing';

import { OutboundQueueService } from './outbound-queue.service';

describe('OutboundQueueService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OutboundQueueService]
    });
  });

  it('should be created', inject([OutboundQueueService], (service: OutboundQueueService) => {
    expect(service).toBeTruthy();
  }));
});
