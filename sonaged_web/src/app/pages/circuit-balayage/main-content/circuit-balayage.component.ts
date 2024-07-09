import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs';
import { succesAlert, errorAlert } from '../../../services/alert.service';
import { SharedService } from '../../../services/shared.service';


@Component({
  selector: 'app-circuit-balayage',
  templateUrl: './circuit-balayage.component.html',
  styleUrl: './circuit-balayage.component.scss'
})
export class CircuitBalayageComponent {

}
