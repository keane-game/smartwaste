import {CommonModule, NgOptimizedImage} from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { HeaderComponent } from './components/header/header.component';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MAT_MATERIAL } from '../materials/material.module';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { HttpClient } from '@angular/common/http';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { CoreModule } from 'keycloak-angular';
import { PaginationCustumerComponent } from './components/pagination-custumer/pagination-custumer.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { LayoutComponent } from './components/layout/layout.component';
import { ErrorComponent } from './components/error/error.component';
import { CustomPaginatorComponent } from './components/custom-paginator/custom-paginator.component';
import { BubblePaginationDirective } from '../directives/BubblePaginationDirective';
import { ModalService } from '../services/modal.service';


const COMPONENTS : any[]= [
    HeaderComponent,
    SidebarComponent,
    LayoutComponent,
    ErrorComponent,
    CustomPaginatorComponent,
    PaginationCustumerComponent,
];

const BASE_MODULES = [
    RouterModule,
    RouterOutlet,
    CommonModule,
    FormsModule,
    CoreModule,
    ReactiveFormsModule,
    TranslateModule,

    BubblePaginationDirective
  ];




  @NgModule({
    declarations: [...COMPONENTS],
    exports: [...COMPONENTS, ...BASE_MODULES,...MAT_MATERIAL],
    imports: [...BASE_MODULES, ...MAT_MATERIAL, NgOptimizedImage],
    providers: [ModalService]
  })
export class SharedModule { }
