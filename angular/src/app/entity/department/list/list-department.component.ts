import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des départements — liste + création/édition inline + suppression.
 *
 * S'appuie sur le client générique {@link SharedService} branché sur `/v1/departments`
 * (liste `GET /departments`, CRUD complet). Aucun modèle typé : le backend renvoie du JSON brut.
 */
@Component({
  selector: 'app-list-department',
  templateUrl: './list-department.component.html',
  styleUrls: ['./list-department.component.scss']
})
export class ListDepartmentComponent implements OnInit {

  private readonly resource = '/departments';

  items: any[] = [];
  regions: any[] = [];
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
    this.headerTitleService.setTitle('Gestion Département');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      region: [null]
    });
    this.load();
    this.loadRegions();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.resource;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des départements.'; }
    });
  }

  /** Régions pour le menu déroulant (rattachement d'un département à sa région). */
  loadRegions(): void {
    this.sharedService.url = '/regions';
    this.sharedService.getAll().subscribe({
      next: (data) => { this.regions = data || []; },
      error: () => { /* non bloquant */ }
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
    this.editingId = item.departmentId;
    this.form.patchValue({
      name: item.name,
      code: item.code,
      region: item.region ?? null
    });
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
        this.message = this.editingId ? 'Département mis à jour.' : 'Département créé.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer le département « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.departmentId).subscribe({
      next: () => { this.message = 'Département supprimé.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
