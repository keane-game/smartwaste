import { Component, Inject } from '@angular/core';
import { SharedService } from '../../../services/shared.service';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';
import { API_ENDPOINTS } from '../../../shared/constants/api-endpoints';

@Component({
  selector: 'app-delete-quartier',
  templateUrl: './delete-quartier.component.html',
  styleUrl: './delete-quartier.component.scss'
})
export class DeleteQuartierComponent {

  id:any;
  error: any;
  constructor(
    @Inject(MAT_DIALOG_DATA) public data:any,
    private sharedService: SharedService,
  ) {
    this.id= data.id
    console.log(data)
   }

   onSubmit() {
    this.sharedService.url = API_ENDPOINTS.quartiers.basePath;
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

