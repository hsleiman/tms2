import { Injectable } from '@angular/core';
import { Config } from '../../app.config';
import { HttpClient } from '../../lib/http-client';
import Disposition from './Disposition';
import DispositionGroup from './DispositionGroup';

@Injectable()
export class DispositionService {
    private baseUrl = Config.Base_URL;

    constructor(private httpClient: HttpClient) {
        this.createDispitionGroups();
    }

    private dispositionGroups: Array<DispositionGroup> = [];

    getDispositionGroups(): Promise<Array<DispositionGroup>> {
        return new Promise((resolve, reject) => {
            return resolve(this.dispositionGroups);
        });
    }

    createDispitionGroups() {
        for (let i = 1; i < 3; i++) {
            let group: DispositionGroup = new DispositionGroup();
            group.pk = i;
            group.createdBy = "admin";
            group.createTime = new Date();
            group.description = "";
            group.name = "test group " + i.toString();

            this.dispositionGroups.push(group);
        }
    }

    getDispositions(): Promise<Array<Disposition>> {
        let dispositions: Array<Disposition> = new Array();

        for (let i = 1; i < 3; i++) {
            let disposition: Disposition = new Disposition();
            disposition.disposition = "Test disposition " + i.toString();
            disposition.dispositionId = i;
            dispositions.push(disposition);
        }

        return new Promise((resolve, reject) => {
            return resolve(dispositions);
        });
    }

    getAllDispositionGroups(): Promise<Array<DispositionGroup>> {
        return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllCallDispositionGroups`, null);
    }

    getAllCallDispositionCodes() : Promise<Array<Disposition>> {
        return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllCallDispositionCodes`, null);
    }

    getAllCallDispositionsForGroup(dispositionGroupPk: number) {
        return this.httpClient.get(`${this.baseUrl}/rest/tmsRestController/getAllCallDispositionsForGroup/${dispositionGroupPk}`, null);
    }

    addOrUpdateCallDispositionGroup(dispositionGroup: DispositionGroup) {
        return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/createOrUpdateDispositionGroup`, dispositionGroup, null);
    }
    
    addCallDispositionCodesToGroup(dispositionGroupPk: number, callDispositionIds: Array<number>) {
        return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/setCallDispositionsToGroup/${dispositionGroupPk}`, callDispositionIds, null);
    }

    addOrUpdateCallDisposition(disposition: Disposition) {
        return this.httpClient.post(`${this.baseUrl}/rest/tmsRestController/addOrUpdateCallDisposition`, disposition, null);
    }

    private getDispositionsInGroup(groupPk: number) {

    }

}
