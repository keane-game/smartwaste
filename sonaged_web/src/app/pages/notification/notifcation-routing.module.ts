import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotifcationComponent } from './notifcation.component';

const routes: Routes = [{ path: '', component: NotifcationComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotifcationRoutingModule { }
