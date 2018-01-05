import { Injectable, EventEmitter } from '@angular/core';
import { Http, Headers, Response, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { HttpHelper } from './http-helpers';


@Injectable()
export class HttpClient {
  public loading: EventEmitter<boolean> = new EventEmitter();
  public error: EventEmitter<any> = new EventEmitter();
  private token: string;
  private username: string;
  constructor(private http: Http, private httpHelper: HttpHelper) {
  }

  setToken(token: string) {
    if (token) {
      localStorage.setItem('token', token);
      this.token = token;
    }
  }
  
  getUsername() {
    return this.username;
  }

  setUsername(username: string) {
    if (username) {
      localStorage.setItem('username', username);
      this.username = username;
    }
  }
  createAuthorizationHeader(headers: Headers) {
    const token: string = this.token;
    const username = this.username;
    if (token && username) {
      headers.append('tokenkey', token);
      if (!headers.get('username')) {
        headers.append('username', username);
      }
    }
    return headers;
  }

  get(url, extraHeaders) {
    let headers = new Headers();
    if (extraHeaders) {
      extraHeaders.forEach(element => {
        headers.append(element.key, element.value);
      });
    }
    headers = this.createAuthorizationHeader(headers);
    const options = new RequestOptions({ headers: headers });
    return this.http.get(url, options).toPromise().then((result) => {
      return this.httpHelper.extractData(result);
    }, (error: Response | any) => {
      this.error.emit(error);
      console.log('error');
      console.log(error);
      if (error.status === 401) {

      }
      return (error.__body);
    });
  }
    delete(url, extraHeaders) {
    let headers = new Headers();
    if (extraHeaders) {
      extraHeaders.forEach(element => {
        headers.append(element.key, element.value);
      });
    }
    headers = this.createAuthorizationHeader(headers);
    const options = new RequestOptions({ headers: headers });
    return this.http.delete(url, options).toPromise().then((result) => {
      return this.httpHelper.extractData(result);
    }, (error: Response | any) => {
      this.error.emit(error);
      console.log('error');
      console.log(error);
      if (error.status === 401) {

      }
      return (error.__body);
    });
  }

  post(url, data, extraHeaders) {
    this.loading.emit(true);
    let headers: Headers = new Headers();
    if (extraHeaders) {
      extraHeaders.forEach(element => {
        headers.append(element.key, element.value);
      });
    }
    if (!headers.get('tokenkey')) {
      console.log(headers.get('tokenkey'));
      headers = this.createAuthorizationHeader(headers);
    }
    const options = new RequestOptions({ headers: headers });

    return this.http.post(url, data, options).toPromise().then((result) => {
      this.loading.emit(false);
      return this.httpHelper.extractData(result);
    }, (error) => {
      this.error.emit(error);
      return this.httpHelper.handleError(error);
    });
  }
}
