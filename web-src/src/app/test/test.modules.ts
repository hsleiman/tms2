import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MaterialImports} from '../shared-modules/app.material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { HttpClient } from '../lib/http-client';
import { HttpHelper } from '../lib/http-helpers';
import { Http,HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';

@NgModule({
    imports:[MaterialImports,FormsModule,HttpModule, RouterModule, BrowserAnimationsModule, ReactiveFormsModule],
    providers:[AuthService,HttpClient, HttpHelper],
    exports:[MaterialImports,FormsModule,HttpModule, RouterModule, BrowserAnimationsModule, ReactiveFormsModule]
})

export class testModules{}