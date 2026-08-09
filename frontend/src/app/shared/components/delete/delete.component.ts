import { Component, Inject } from '@angular/core';
import { SharedService } from '../../../services/shared.service';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';

@Component({
    selector: 'app-delete',
    templateUrl: './delete.component.html',
    styleUrl: './delete.component.scss',
    standalone: false
})
export class DeleteComponent {

  id:any;
  error: any;
  url: ""
  constructor(
    @Inject(MAT_DIALOG_DATA) public data:any,
    private sharedService: SharedService,
  ) {
    this.id = data.id
    this.url = data.url
    console.log(data)
   }

   onSubmit() {
    this.sharedService.url = this.url;
    //console.log(this.data.id)
    this.sharedService.delete(this.id)
      .pipe(first())
      .subscribe({
        next:  () => {
          succesAlert("La suppression a bien réussi")
        },
        error: (error) => {
          errorAlert('Erreur' + error.message)
        }
      })
  }
}
