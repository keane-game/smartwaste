import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { EntityListComponent } from './entity-list/entity-list.component';
import { EntityFormComponent } from './entity-form/entity-form.component';

/**
 * Composants CRUD génériques, partagés par toutes les ressources de `ENTITY_CONFIGS`.
 *
 * Exportés pour que chaque module d'entité puisse les réutiliser sans les redéclarer
 * (un composant ne peut être déclaré que dans un seul NgModule).
 */
@NgModule({
  declarations: [
    EntityListComponent,
    EntityFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule
  ],
  exports: [
    EntityListComponent,
    EntityFormComponent
  ]
})
export class CrudModule { }

/**
 * Construit le jeu de routes standard d'une ressource : liste, création, édition.
 *
 * Évite de recopier la même structure dans les douze modules d'entité ; la clé passée est
 * celle du registre `ENTITY_CONFIGS`, transmise aux composants via `data.entityKey`.
 */
export function crudRoutes(entityKey: string): Routes {
  return [
    { path: '', component: EntityListComponent, data: { entityKey } },
    { path: 'create', component: EntityFormComponent, data: { entityKey } },
    { path: ':id/edit', component: EntityFormComponent, data: { entityKey } }
  ];
}
