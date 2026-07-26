import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des régions — liste + création.
 *
 * Le backend `/v1/regions` n'expose que la lecture (`GET`) et la création (`POST`) :
 * ni mise à jour ni suppression ne sont donc proposées ici.
 */
@Component({
  selector: 'app-list-region',
  templateUrl: './list-region.component.html',
  styleUrls: ['./list-region.component.scss']
})
export class ListRegionComponent implements OnInit {

  private readonly resource = '/regions';

  items: any[] = [];
  loading = false;
  saving = false;
  message = '';
  error = '';

  form!: FormGroup;
  showForm = false;

  constructor(
    private sharedService: SharedService,
    private formBuilder: FormBuilder,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Gestion Région');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required]
    });
    this.load();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.resource;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des régions.'; }
    });
  }

  openCreate(): void {
    this.form.reset();
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  cancel(): void {
    this.showForm = false;
    this.form.reset();
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.sharedService.url = this.resource;
    this.sharedService.create(this.form.value).subscribe({
      next: () => {
        this.saving = false;
        this.message = 'Région créée.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }
}
