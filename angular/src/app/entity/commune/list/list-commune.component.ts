import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des communes — liste + création/édition inline + suppression.
 *
 * Backend `/v1/communes` : la liste complète est servie par `GET /communes/s`
 * (le `GET /communes` racine est paginé), le CRUD par les routes standard.
 */
@Component({
  selector: 'app-list-commune',
  templateUrl: './list-commune.component.html',
  styleUrls: ['./list-commune.component.scss']
})
export class ListCommuneComponent implements OnInit {

  private readonly resource = '/communes';
  private readonly listUrl = '/communes/s';

  items: any[] = [];
  departments: any[] = [];
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
    this.headerTitleService.setTitle('Gestion Commune');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: ['', Validators.required],
      total: [''],
      women: [''],
      men: [''],
      length: [''],
      area: [''],
      department: [null]
    });
    this.load();
    this.loadDepartments();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.listUrl;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des communes.'; }
    });
  }

  loadDepartments(): void {
    this.sharedService.url = '/departments';
    this.sharedService.getAll().subscribe({
      next: (data) => { this.departments = data || []; },
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
    this.editingId = item.communeId;
    this.form.patchValue({
      name: item.name,
      code: item.code,
      total: item.total,
      women: item.women,
      men: item.men,
      length: item.length,
      area: item.area,
      department: item.department ?? null
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
        this.message = this.editingId ? 'Commune mise à jour.' : 'Commune créée.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer la commune « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.communeId).subscribe({
      next: () => { this.message = 'Commune supprimée.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
