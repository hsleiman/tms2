import { Injectable } from '@angular/core';
import { GroupI} from '../group/group.service';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';

@Injectable()
export class GroupsService {
   private baseUrl = Config.Base_URL;
  constructor(private httpClient: HttpClient) { }

 public getDialerGroups(): Promise<Array<GroupI>> {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllDialerGroups`, null);
  }

  public addGroup(group: GroupI) {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/createOrUpdateDialerGroup`, group, null);
  }
}
