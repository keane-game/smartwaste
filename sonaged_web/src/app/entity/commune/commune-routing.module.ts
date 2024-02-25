import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommuneComponent } from './commune.component';
import { CreateCommuneComponent } from './create-commune/create-commune.component';

const routes: Routes = [
    { path: '', component: CommuneComponent },
    { path: '', component: CreateCommuneComponent },
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommuneRoutingModule { }
