import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { RouterModule, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { DebugElement } from '@angular/core';
import { NgForm } from '@angular/forms';

import { testModules } from '../../test/test.modules';
import { LoginComponent } from './login.component';

let comp: LoginComponent;
let fixture: ComponentFixture<LoginComponent>;
let page: Page;

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockRouter = {
    navigate: jasmine.createSpy('navigate')
  }
  let page: Page;


  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [testModules],
      providers: [
        { provide: Router, useValue: mockRouter },NgForm
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('empty form should be invalid', () => {
    fixture.detectChanges();
    let form: NgForm = fixture.debugElement.children[0].injector.get(NgForm);
    expect (form.control.invalid).toEqual(false);
  });
});

class Page {
  gotoSpy: jasmine.Spy;
  navSpy: jasmine.Spy;

  saveBtn: DebugElement;
  username: DebugElement;
  password: DebugElement;
  form: DebugElement;
  nameDisplay: HTMLElement;
  nameInput: HTMLInputElement;

  constructor() {
    const router = TestBed.get(Router); // get router from root injector
    this.navSpy = spyOn(router, 'navigate');
  }

  /** Add page elements after hero arrives */
  addPageElements() {
    // have a hero so these elements are now in the DOM
    const buttons = fixture.debugElement.queryAll(By.css('button'));
    const inputs = fixture.debugElement.queryAll(By.css('input'));
    this.form = fixture.debugElement.query(By.css('form'));
    this.saveBtn = buttons[0];
    this.username = inputs[0].nativeElement;
    this.password = inputs[1].nativeElement;
    this.nameDisplay = fixture.debugElement.query(By.css('span')).nativeElement;
    this.nameInput = fixture.debugElement.query(By.css('input')).nativeElement;
  }
}
