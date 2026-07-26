import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrudModule } from '../../shares/crud/crud.module';
import { GeometryRoutingModule } from './geometry-routing.module';

/**
 * Module « Geometry ».
 *
 * Cette ressource était exposée par le backend (`/v1/geometries`) sans aucun écran côté
 * frontend : elle est désormais couverte par les composants CRUD génériques.
 */
@NgModule({
  imports: [
    CommonModule,
    CrudModule,
    GeometryRoutingModule
  ]
})
export class GeometryModule { }
