import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';

@Injectable()
export class RolesService {

  constructor(private httpClient: HttpClient) { }
  private baseUrl = Config.Base_URL;
  getRoles():Promise< any > {
   return this.httpClient.get(`${this.baseUrl}/rest/amsRestController/getAllPermissions`, null);
  }

}
