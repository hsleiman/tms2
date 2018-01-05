import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';

export class TmsAgentI {
  firstName: string;
  lastName: string;
  userName: string;
  createdDate?: Array<number>;
  phoneExtension: number;
}

@Injectable()
export class AgentsService {
private baseUrl = Config.Base_URL;
  constructor(private httpClient: HttpClient) { }

  getAgents(): Promise<Array<TmsAgentI>> {
   return this.httpClient.get(`${this.baseUrl}/rest/amsRestController/getAllAgents`, null);
  }

}
