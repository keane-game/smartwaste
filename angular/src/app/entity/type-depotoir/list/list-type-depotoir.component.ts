import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des types de dépotoir — liste + création/édition inline + suppression.
 * Backend `/v1/typedepotoirs` (liste `GET`, CRUD complet).
 */
@Component({
  selector: 'app-list-type-depotoir',
  templateUrl: './list-type-depotoir.component.html',
  styleUrls: ['./list-type-depotoir.component.scss']
})
export class ListTypeDepotoirComponent implements OnInit {

  private readonly resource = '/typedepotoirs';

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
    this.headerTitleService.setTitle('Gestion Type de dépotoir');
    this.form = this.formBuilder.group({
      name: ['', Validators.required]
    });
    this.load();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.resource;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des types de dépotoir.'; }
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
    this.editingId = item.typeDepotoirId;
    this.form.patchValue({ name: item.name });
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
        this.message = this.editingId ? 'Type mis à jour.' : 'Type créé.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer le type « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.typeDepotoirId).subscribe({
      next: () => { this.message = 'Type supprimé.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
