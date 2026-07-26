import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { first } from 'rxjs';
import { SharedService } from '../../../services/shared.service';

/**
 * Création d'un dépotoir — formulaire réel (remplace l'ancien stub).
 *
 * `POST /v1/depotoirs` via {@link SharedService}. Le type et la commune sont sélectionnés
 * depuis les référentiels (`/typedepotoirs`, `/communess`) ; le quartier est référencé par id.
 */
@Component({
  selector: 'app-create-depotoir',
  templateUrl: './create-depotoir.component.html',
  styleUrls: ['./create-depotoir.component.scss']
})
export class CreateDepotoirComponent implements OnInit {

  private readonly resource = '/depotoirs';

  form!: FormGroup;
  types: any[] = [];
  communes: any[] = [];
  saving = false;
  error = '';

  constructor(
    private sharedService: SharedService,
    private formBuilder: FormBuilder,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.form = this.formBuilder.group({
      address: ['', Validators.required],
      quartierId: [null],
      typeDepotoir: [null],
      commune: [null]
    });
    this.loadTypes();
    this.loadCommunes();
  }

  get f() { return this.form.controls; }

  loadTypes(): void {
    this.sharedService.url = '/typedepotoirs';
    this.sharedService.getAll().subscribe({ next: (d) => this.types = d || [], error: () => {} });
  }

  loadCommunes(): void {
    this.sharedService.url = '/communess';
    this.sharedService.getAll().subscribe({ next: (d) => this.communes = d || [], error: () => {} });
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.sharedService.url = this.resource;
    this.sharedService.create(this.form.value).pipe(first()).subscribe({
      next: () => { this.saving = false; this.router.navigate(['/depotoirs']); },
      error: () => { this.saving = false; this.error = 'Échec de la création du dépotoir.'; }
    });
  }

  cancel(): void {
    this.router.navigate(['/depotoirs']);
  }
}
