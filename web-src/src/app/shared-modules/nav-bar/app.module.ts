import { NgModule } from '@angular/core';
import { BrowserModule, Title } from '@angular/platform-browser';
import { NavBarComponent } from './nav-bar.component';
import { MaterialImports } from '../app.material';
import { RouterModule } from '@angular/router';

@NgModule({ declarations: [NavBarComponent],
    exports:[NavBarComponent],
    imports:[MaterialImports, MaterialImports, BrowserModule, RouterModule] })

export class NavBar { }