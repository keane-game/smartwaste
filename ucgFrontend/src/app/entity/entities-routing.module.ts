import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CompetencesComponent } from './competences/competences.component';
import { CreateCompetencesComponent } from './competences/create-competences/create-competences.component';
import { CreateGrpcompetencesComponent } from './grp-competences/create-grpcompetences/create-grpcompetences.component';
import { GrpCompetencesComponent } from './grp-competences/grp-competences.component';
import { CreateGrptagsComponent } from './grp-tags/create-grptags/create-grptags.component';
import { GrpTagsComponent } from './grp-tags/grp-tags.component';
import { ProfilSortisComponent } from './profil-sortis/profil-sortis.component';
import { ProfilDetailComponent } from './profil/profil-detail/profil-detail.component';
import { ProfilComponent } from './profil/profil.component';
import { CreatePromosComponent } from './promos/create-promos/create-promos.component';
import { DetailPromosComponent } from './promos/detail-promos/detail-promos.component';
import { PromosComponent } from './promos/promos.component';
import { CreateReferencielComponent } from './referenciels/create-referenciel/create-referenciel.component';
import { DetailReferencielComponent } from './referenciels/detail-referenciel/detail-referenciel.component';
import { ReferencielsComponent } from './referenciels/referenciels.component';
import { CreateUserComponent } from './users/create-user/create-user.component';
import { DetailUserComponent } from './users/detail-user/detail-user.component';
import { ListUserComponent } from './users/list-user/list-user.component';

const routes: Routes = [

    {
        path: 'profil',
        component: ProfilComponent,
        loadChildren: () => import('src/app/entities/profil/profil.module').then(m => m.ProfilModule)
    },

    {
        path: 'list-profil', component: ProfilComponent,
        loadChildren: () => import('src/app/entities/profil/profil.module').then(m => m.ProfilModule)
    },
    {
        path: 'detail-profil/:id', component: ProfilDetailComponent,
        loadChildren: () => import('src/app/entities/profil/profil.module').then(m => m.ProfilModule)
    },
    {
        path: 'user-list', component: ListUserComponent,
        loadChildren: () => import('src/app/entities/users/user.module').then(m => m.UserModule)
    },
    {
        path: 'create-user', component: CreateUserComponent,
        loadChildren: () => import('src/app/entities/users/user.module').then(m => m.UserModule)
    },
    {
        path: 'edit-user/:id', component: CreateUserComponent,
        loadChildren: () => import('src/app/entities/users/user.module').then(m => m.UserModule)
    },
    {
        path: 'detail-user/:id', component: DetailUserComponent,
        loadChildren: () => import('src/app/entities/users/user.module').then(m => m.UserModule)
    },
    {
        path: 'profil-sortis', component: ProfilSortisComponent,
        loadChildren: () => import('src/app/entities/profil-sortis/profil-sortis.module').then(m => m.ProfilSortisModule)
    },
    {
        path: 'promos', component: PromosComponent,
        loadChildren: () => import('src/app/entities/promos/promos.module').then(m => m.PromosModule)
    },
    {
        path: 'create-promos', component: CreatePromosComponent,
        loadChildren: () => import('src/app/entities/promos/promos.module').then(m => m.PromosModule)
    },
    {
        path: 'detail-promos', component: DetailPromosComponent,
        loadChildren: () => import('src/app/entities/promos/promos.module').then(m => m.PromosModule)
    },
    {
        path: 'referenciel', component: ReferencielsComponent,
        loadChildren: () => import('src/app/entities/referenciels/referenciel.module').then(m => m.ReferencielModule)
    },
    {
        path: 'create-referenciel', component: CreateReferencielComponent,
        loadChildren: () => import('src/app/entities/referenciels/referenciel.module').then(m => m.ReferencielModule)
    },
    {
        path: 'detail-referenciel', component: DetailReferencielComponent,
        loadChildren: () => import('src/app/entities/referenciels/referenciel.module').then(m => m.ReferencielModule)
    },
    {
        path: 'grp-tags', component: GrpTagsComponent,
        loadChildren: () => import('src/app/entities/grp-tags/grptags.module').then(m => m.GrptagsModule)
    },
    {
        path: 'create-grptags', component: CreateGrptagsComponent,
        loadChildren: () => import('src/app/entities/grp-tags/grptags.module').then(m => m.GrptagsModule)
    },
    {
        path: 'grp-competence', component: GrpCompetencesComponent ,
        loadChildren: () => import('src/app/entities/grp-competences/grpcompetence.module').then(m => m.GrpcompetenceModule)
    },
    {
        path: 'create-grpcompetence', component: CreateGrpcompetencesComponent ,
        loadChildren: () => import('src/app/entities/grp-competences/grpcompetence.module').then(m => m.GrpcompetenceModule)
    },
    {
        path: 'comptence', component: CompetencesComponent ,
        loadChildren: () => import('src/app/entities/competences/competence.module').then(m => m.CompetenceModule)
    },
    {
        path: 'create-comptence', component: CreateCompetencesComponent ,
        loadChildren: () => import('src/app/entities/competences/competence.module').then(m => m.CompetenceModule)
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
exports: [RouterModule]
})
export class EntitiesRoutingModule { }
