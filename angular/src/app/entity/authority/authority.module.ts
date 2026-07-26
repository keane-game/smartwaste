import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CrudModule } from '../../shares/crud/crud.module';
import { AuthorityRoutingModule } from './authority-routing.module';

/**
 * Module « Authority » (rôles).
 *
 * L'écran précédent se limitait à une liste en lecture (`ListAuthorityComponent`, conservé sur
 * disque mais plus routé) ; la ressource passe aux composants CRUD génériques, qui ajoutent
 * création, modification, suppression, recherche, tri et pagination.
 */
@NgModule({
  imports: [
    CommonModule,
    CrudModule,
    AuthorityRoutingModule
  ]
})
export class AuthorityModule { }
