import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';
import { log } from 'util';
import * as _ from 'lodash';

@Injectable()
export class CallHistoryService {

  agents: Array<CallI>;

  private baseUrl = Config.Base_URL;
  constructor(private httpClient: HttpClient) { }

  getAgents(): Promise<Array<CallI>> {
    return this.httpClient.get(`${this.baseUrl}/rest/amsRestController/getAllAgents`, null).then((result: Array<CallI>) => {
      console.log(result);
      this.agents = result;
      return result;
    });
  }

}

export class CallI {
  userName: string;
  firstName: string;
  lastName: string;
  phoneNumber: number;
}
