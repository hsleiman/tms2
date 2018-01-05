import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DispositionsComponent } from './dispositions.component';

describe('DispositionsComponent', () => {
  let component: DispositionsComponent;
  let fixture: ComponentFixture<DispositionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DispositionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DispositionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
