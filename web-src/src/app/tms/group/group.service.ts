import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';
import { TmsAgentI } from '../agents/agents.service';

export class GroupI {
  pk: number;
  groupName: string;
  leaderName: string;
  lastChangedBy: string;
  isActive: boolean;
  comment: string;
  dialerGroupAgents: Array<TmsAgentI>;
}
@Injectable()
export class GroupService {
  private baseUrl = Config.Base_URL;
  constructor(private httpClient: HttpClient) { }
  getGroupById(id: number) {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getDialerGroup/${id}`, null);
  }
  getAgentsForGroup(id: number) {
    return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllAgentsInDialerGroup/${id}`, null);
  }
  assignAgentsToGroup(groupPk, agents) {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/assignAgentsToDialerGroup/${groupPk}`, agents, null);
  }

  deleteAgentToGroup(groupPk, username) {
    return this.httpClient.delete(`${this.baseUrl}/rest/tmsRestController/deleteAgentFromDialerGroup/${groupPk}/${username}`, null);
  }

  updateAgentToGroup(groupPk, agents) {
    return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/updateAgentsToDialerGroup/${groupPk}/`,agents, null);
  }

  
}
