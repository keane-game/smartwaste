import { NgModule } from '@angular/core';
import { UsersRoutingModule } from './users-routing.module';
import { CreateUserComponent } from './create-user/create-user.component';
import { UserComponent } from './main-content/user.component';
import { UpdateUserComponent } from './update-user/update-user.component';
import { SharedModule } from '../../shared/shared.module';
import { DeleteUserComponent } from './delete-user/delete-user.component';

@NgModule({
 
 
  declarations: [
    CreateUserComponent,
    UpdateUserComponent,
    DeleteUserComponent,
    UserComponent,
  ],
  imports: [
    SharedModule,
    UsersRoutingModule,
  ],
})
export class UserModule { }
