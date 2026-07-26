import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion du mobilier urbain — liste + création/édition inline + suppression.
 * Backend `/v1/moblier-urbains` (liste `GET`, CRUD complet).
 */
@Component({
  selector: 'app-list-moblier-urbain',
  templateUrl: './list-moblier-urbain.component.html',
  styleUrls: ['./list-moblier-urbain.component.scss']
})
export class ListMoblierUrbainComponent implements OnInit {

  private readonly resource = '/moblier-urbains';

  items: any[] = [];
  loading = false;
  saving = false;
  message = '';
  error = '';

  form!: FormGroup;
  showForm = false;
  editingId: number | null = null;

  constructor(
    private sharedService: SharedService,
    private formBuilder: FormBuilder,
    private headerTitleService: headerTitleService
  ) { }

  ngOnInit(): void {
    this.headerTitleService.setTitle('Gestion Mobilier Urbain');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['']
    });
    this.load();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.resource;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement du mobilier urbain.'; }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset();
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  openEdit(item: any): void {
    this.editingId = item.moblierUrbainId;
    this.form.patchValue({ name: item.name, code: item.code });
    this.showForm = true;
    this.message = '';
    this.error = '';
  }

  cancel(): void {
    this.showForm = false;
    this.editingId = null;
    this.form.reset();
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.sharedService.url = this.resource;
    const payload = this.form.value;
    const request = this.editingId
      ? this.sharedService.update(payload, this.editingId)
      : this.sharedService.create(payload);
    request.subscribe({
      next: () => {
        this.saving = false;
        this.message = this.editingId ? 'Mobilier urbain mis à jour.' : 'Mobilier urbain créé.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer le mobilier urbain « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.moblierUrbainId).subscribe({
      next: () => { this.message = 'Mobilier urbain supprimé.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
