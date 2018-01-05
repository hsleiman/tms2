import { Injectable } from '@angular/core';
import { HttpClient } from '../../lib/http-client';
import { Config } from '../../app.config';
import { log } from 'util';
import * as _ from 'lodash';

@Injectable()
export class AgentsService {

  agents: Array<AgentI>;

  private baseUrl = Config.Base_URL;
  constructor(private httpClient: HttpClient) { }

  getAgents(): Promise<Array<AgentI>> {
    return this.httpClient.get(`${this.baseUrl}/rest/amsRestController/getAllAgents`, null).then((result: Array<AgentI>) => {
      console.log(result);
      this.agents = result;
      return result;
    });
  }

  getAgent(agentPk: number): Promise<AgentI> {
    if (this.agents != null && this.agents.length > 0) {
      let cachedAgent = _.find(this.agents, agent => {
        return (agent.pk === agentPk);
      });
      if (cachedAgent != null) {
        return Promise.resolve(cachedAgent);
      }
    }
    let url = this.baseUrl + "/rest/amsRestController/getAgentByPk/" + agentPk.toString(10);
    return this.httpClient.get(url, null);
  }

  public addAgent(agent: AgentI) {
    return this.httpClient.post(`${this.baseUrl}/rest/amsRestController/createAmsUser`, agent, null);
  }

  updateAgentInfo(newInfo: UpdateAgentInfoVM) {
    let url = this.baseUrl + "/rest/amsRestController/updateAmsUser";
    return this.httpClient.post(url, newInfo, null);
  }

  updateAgentSecurity(newSecurity: UpdateAgentSecurityVM) {
    let url = this.baseUrl + "/rest/amsRestController/addPermissionToUser";
    return this.httpClient.post(url, newSecurity, null);
  }

  getAllPermissions(): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getAllPermissions";
    return this.httpClient.get(url, null);
  }

  getAllRoles(): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getAllRoles";
    return this.httpClient.get(url, null);
  }

  getRolePermissions(roleId: number): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getRolePermissions/" + roleId.toString(10);
    return this.httpClient.get(url, null);
  }

  getPermissionRoles(permissionId: number): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getAllRolesWithPermission/" + permissionId.toString(10);
    return this.httpClient.get(url, null);
  }

  getAgentPermissions(username: string): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getUserPermissions/" + username;
    return this.httpClient.get(url, null);
  }

  getAgentRoles(username: string): Promise<Array<any>> {
    let url = this.baseUrl + "/rest/amsRestController/getUserRoles/" + username;
    return this.httpClient.get(url, null);
  }

  confirmPassword(credentials: ChangePasswordVM): Promise<boolean> {
    let url = this.baseUrl + "/rest/amsRestController/agentPasswordTest";
    return this.httpClient.post(url, credentials, null);
  }

  changePassword(credentials: ChangePasswordVM) {
    let url = this.baseUrl + "/rest/amsRestController/changeAgentPassword";
    return this.httpClient.post(url, credentials, null);
  }
}

export class AgentI {
  pk: number;
  isActive: any;
  userName: string;
  firstName: string;
  lastName: string;
  emailAddress: string;
  phoneNumber: number;
  phoneExtension: number;
}

export class UpdateAgentInfoVM {
  userName: string;
  firstName: string;
  lastName: string;
  emailAddress: string;
  phoneNumber: any;
  extension: any;
  voiceMailPassword: any; 
  status: any;
}

export class ChangePasswordVM {
  agentUsername: string;
  agentPassword: string;
  newAgentPassword: string;
}

export class UpdateAgentSecurityVM {
  agentUsername: string;
  roleIds: Array<number>;
  includePermissionIds: Array<number>;
  excludePermissionIds: Array<number>;
}