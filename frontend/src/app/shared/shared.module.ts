import {CommonModule, NgOptimizedImage} from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MAT_MATERIAL } from './materials/material.module';
import { TranslateModule } from '@ngx-translate/core';
import { CoreModule } from 'keycloak-angular';
import { PaginationCustumerComponent } from './components/pagination-custumer/pagination-custumer.component';
import { ErrorComponent } from './components/error/error.component';

import { BubblePaginationDirective } from '../directives/BubblePaginationDirective';
import { ModalService } from '../services/modal.service';
import { DeleteComponent } from './components/delete/delete.component';


// `HeaderComponent`/`LayoutComponent`/`SidebarComponent` sont sortis de ce module : les router
// routes vers un composant declare dans un NgModule (`component: LayoutComponent` dans
// app.routes.ts) ne recoit jamais la portee de directives de ce NgModule dans un bootstrap
// standalone (`bootstrapApplication`) — `LayoutComponent.ɵcmp.directiveDefs` restait `null` a
// l'execution, donc `<app-sidebar>`/`<app-header>` ne s'instanciaient jamais (page blanche
// silencieuse, aucune erreur), trouve en verifiant l'app en conditions reelles (Phase 2). Les
// trois sont maintenant standalone et s'importent directement dans layout.component.ts.
const COMPONENTS : any[]= [
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
  ];




  @NgModule({
    declarations: [...COMPONENTS],
    exports: [...COMPONENTS, ...BASE_MODULES,...MAT_MATERIAL],
    imports: [...BASE_MODULES, ...MAT_MATERIAL, NgOptimizedImage],
    providers: [ModalService]
  })
export class SharedModule { }
