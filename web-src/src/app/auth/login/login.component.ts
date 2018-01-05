import { Component, OnInit } from '@angular/core';
import { LoginI, AuthService } from '../auth.service';
import { HttpClient } from '../../lib/http-client';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  constructor(private authService: AuthService, private httpClient: HttpClient, private route: Router) { }
  public login = new LoginI();
  ngOnInit() {
  }

  loginCont() {
    this.authService.login(this.login.username, this.login.password).then((result) => {
      this.httpClient.setToken(result.tokenKey);
      this.httpClient.setUsername(this.login.username);
      this.authService.setIsAuth(true);
      this.authService.getIsAuth().then(() => { this.route.navigate(['']); });
    }, (error) => {
      console.log(error);
    });
  }

}
