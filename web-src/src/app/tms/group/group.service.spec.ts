import {   async, ComponentFixture, fakeAsync, inject, TestBed, tick } from '@angular/core/testing';

import { GroupComponent } from './group.component';

import { GroupService } from './group.service';


 let fixture: ComponentFixture<GroupComponent>;
 let comp: GroupComponent;
describe('GroupService', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [GroupService], declarations: [GroupComponent]
    });

     fixture = TestBed.createComponent(GroupComponent);
     comp = fixture.componentInstance;
  }));

  it('should be created', inject([GroupService], (service: GroupService) => {
    expect(service).toBeTruthy();
  }));

  it('should be created', inject([GroupService], (service: GroupService) => {
comp.ngOnInit();
fixture.detectChanges();
expect(comp.errorNotFound).toBe(false);
  }));
});
