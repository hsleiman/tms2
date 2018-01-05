import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-nav-bar',
  templateUrl: './nav-bar.component.html',
  styleUrls: ['./nav-bar.component.scss']
})
export class NavBarComponent implements OnInit {
  constructor() { }
  @Input() navParams;
  public length = 0;
  ngOnInit() {
    
    if (this.navParams)
      this.length = this.navParams.length;
  }

}
