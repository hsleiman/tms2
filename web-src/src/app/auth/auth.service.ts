import { Injectable } from '@angular/core';
import { HttpClient } from '../lib/http-client';
import { Config } from '../app.config';


@Injectable()
export class AuthService {

  private baseUrl = Config.Base_URL;
  private authenticated = false;

  constructor(private httpClient: HttpClient) {
    this.getIsAuth();
  }
  isAuth() {
    return this.authenticated;
  }

  setIsAuth(auth) {
    this.authenticated = auth;
  }

  getIsAuth() {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    if (token) {
      const login = [{
        key: 'username', value: username
      }, {
        key: 'tokenkey', value: token
      }];
      
      return this.httpClient.post(`${this.baseUrl}/auth/validtoken`, '', login).then((result) => {
        return new Promise((resolve, reject) => {
          if (result.auth) {
            this.httpClient.setToken(token);
            this.authenticated = true;
            if (username) {
              this.httpClient.setUsername(username);
            }
            return resolve(true);
          } else {
            this.authenticated = false;
            return resolve(false);
          }
        })
      });
    } else {
      return new Promise((resolve, reject) => {
        resolve(false);
      })
    }
  }

  login(username: string, password: string) {
    const login = [{
      key: 'username', value: username
    }, {
      key: 'password', value: password
    }];

    return this.httpClient.post(`${this.baseUrl}/auth/login`, '', login);
  }

  logout() {
    localStorage.setItem('token', '');
    localStorage.setItem('username', '');
    this.authenticated = false;
  }

  getLoggedInUsername(): string {
    return this.httpClient.getUsername();
  }
}


export class LoginI {
  username: string;
  password: string;
}
