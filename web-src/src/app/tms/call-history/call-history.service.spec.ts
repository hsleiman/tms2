import { TestBed, inject } from '@angular/core/testing';

import { CallHistoryService } from './call-history.service';

describe('AgentsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CallHistoryService]
    });
  });

  it('should be created', inject([CallHistoryService], (service: CallHistoryService) => {
    expect(service).toBeTruthy();
  }));
});
