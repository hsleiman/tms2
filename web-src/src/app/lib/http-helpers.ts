'user strict';
import {Http, Headers, Response} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Injectable } from '@angular/core';

@Injectable()
export class HttpHelper {
  constructor() { }
  extractData(body: Response | any) {
    const result = JSON.parse(body._body);
    return result || {};
  }
  handleError(error: Response | any) {
    // In a real world app, we might use a remote logging infrastructure
    let errMsg: string;
    if (error instanceof Response) {
      const body = error.json() || '';
      const err = body.error || JSON.stringify(body);
      errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
    } else {
      errMsg = error.message ? error.message : error.toString();
    }
    console.log('error');
    console.log(errMsg);
    return Promise.reject(errMsg);
  }
}
