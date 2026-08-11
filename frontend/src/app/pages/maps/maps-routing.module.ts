import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MapsComponent } from './main-content/maps.component';

// `EsriComponent` n'etait jamais atteignable ici : une deuxieme route sur le meme chemin vide
// que `MapsComponent` juste au-dessus ne matche jamais (Angular s'arrete a la premiere
// correspondance). Il reste routable via `/map` (app.routes.ts) — seul chemin qui le sert deja.
const routes: Routes = [
  { path: '', component: MapsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MapsRoutingModule { }
