import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuartierComponent } from './main-content/quartier.component';

const routes: Routes = [
    { path: '', component: QuartierComponent },
    { path: '', component: QuartierComponent }
  ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QuartierRoutingModule { }
