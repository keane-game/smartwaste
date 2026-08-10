import {CommonModule, NgOptimizedImage} from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { HeaderComponent } from './components/header/header.component';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MAT_MATERIAL } from './materials/material.module';
import { TranslateModule } from '@ngx-translate/core';
import { CoreModule } from 'keycloak-angular';
import { PaginationCustumerComponent } from './components/pagination-custumer/pagination-custumer.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { LayoutComponent } from './components/layout/layout.component';
import { ErrorComponent } from './components/error/error.component';

import { BubblePaginationDirective } from '../directives/BubblePaginationDirective';
import { ModalService } from '../services/modal.service';
import { DeleteComponent } from './components/delete/delete.component';


// SidebarComponent est standalone depuis la Phase 2 de la refonte (filtrage de navigation par
// role) : un composant standalone ne peut plus figurer dans `declarations`, il s'importe comme
// un module. Reste expose via `exports` pour que les consommateurs de SharedModule n'aient rien
// a changer.
const COMPONENTS : any[]= [
    HeaderComponent,
    LayoutComponent,
    ErrorComponent,
    PaginationCustumerComponent,
    DeleteComponent
];

const BASE_MODULES = [
    RouterModule,
    RouterOutlet,
    CommonModule,
    FormsModule,
    CoreModule,
    ReactiveFormsModule,
    TranslateModule,
    BubblePaginationDirective,
    SidebarComponent,
  ];




  @NgModule({
    declarations: [...COMPONENTS],
    exports: [...COMPONENTS, ...BASE_MODULES,...MAT_MATERIAL],
    imports: [...BASE_MODULES, ...MAT_MATERIAL, NgOptimizedImage],
    providers: [ModalService]
  })
export class SharedModule { }
