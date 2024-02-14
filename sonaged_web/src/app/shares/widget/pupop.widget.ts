import { NgComponentOutlet, AsyncPipe } from '@angular/common';
import { Component, ElementRef, Renderer2 } from '@angular/core';


@Component({
  selector: 'app-pupop',
  template: `
  <div class="search-filter">
    <div class="mat-form-field">
      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label style="display: flex; flex-direction: row; justify-content: center;">
          <mat-icon style="font-size: larger; margin-right: 0.5rem;">search</mat-icon>
          Recherche
        </mat-label>
        <input matInput (keyup)="($event)" placeholder="" #input>
      </mat-form-field>
    </div>
    <mat-form-field appearance="outline" style="width: 12%;" mat-stroked-button class="btn-filter" >
      <mat-label>
        <mat-icon style="padding-right: 2rem">filter_list</mat-icon>
        Filter
      </mat-label>
      <mat-select>
        <mat-option value="option1">Das</mat-option>
        <mat-option value="option2">Role</mat-option>
        <mat-option value="option3">Email</mat-option>
      </mat-select>
    </mat-form-field>
    <button mat-flat-button class="add-user-btn"  >Ajouter utilisateur</button>
  </div>
   `,
   styles: [`
   // CSS for the search and filter
   .search-filter {
       margin-top: 1%;
       margin-bottom: 2%;
       display: flex;
   }
   
   .search-filter mat-form-field {
       height: 60%;
   }
   
   .btn-filter {
       width: 15rem;
       border-radius: 0.65rem;
       display: flex;
       justify-content: left !important;
   }
   
   .btn-add {
       width: 20%;
       background-color: #5D8B47 !important;
       color: white !important;
       border-radius: 0.3rem;
       height: 55px !important;
       margin-right:10px;
   }
   
   .form-search {
    width: 40%;
    display: flex;
    justify-content: space-between;
}
::ng-deep .mat-mdc-form-field-subscript-wrapper{
    display: none;
}

// CSS for the search and filter

.search-filter {
    height: 3.5rem;
    margin-top: 2%;
    margin-bottom: 2%;
    display: flex;
    justify-content: space-between;
}

.search-filter mat-form-field {
    height: 100%;
}


.btn-filter {
    width: 15%;
    border-radius: 0.65rem;
    display: flex;
    justify-content: left;
}

.add-user-btn {
    width: 20%;
    background-color: #5D8B47 !important;
    color: white !important;
    border-radius: 0.65rem;
}

.mat-form-field {
    width: 60%;
    display: flex;
    justify-content: space-between;
}

mat-form-field,
button {
    height: 3rem !important;
}


   `]
})
export class PuPopWidget {
  message = "";

}