import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrudModule } from '../../shares/crud/crud.module';
import { CoordinateRoutingModule } from './coordinate-routing.module';

/**
 * Module « Coordinate ».
 *
 * Cette ressource était exposée par le backend (`/v1/coordinates`) sans aucun écran côté
 * frontend : elle est désormais couverte par les composants CRUD génériques.
 */
@NgModule({
  imports: [
    CommonModule,
    CrudModule,
    CoordinateRoutingModule
  ]
})
export class CoordinateModule { }
