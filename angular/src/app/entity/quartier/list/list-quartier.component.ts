import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedService } from '../../../services/shared.service';
import { headerTitleService } from '../../../services/headerTitle.service';

/**
 * Gestion des quartiers — liste + création/édition inline + suppression.
 *
 * Backend `/v1/quartiers` : liste complète via `GET /quartiers/s` (racine paginée), CRUD standard.
 */
@Component({
  selector: 'app-list-quartier',
  templateUrl: './list-quartier.component.html',
  styleUrls: ['./list-quartier.component.scss']
})
export class ListQuartierComponent implements OnInit {

  private readonly resource = '/quartiers';
  private readonly listUrl = '/quartiers/s';

  items: any[] = [];
  communes: any[] = [];
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
    this.headerTitleService.setTitle('Gestion Quartier');
    this.form = this.formBuilder.group({
      name: ['', Validators.required],
      code: [''],
      cav: [''],
      codeCav: [''],
      cCrca: [''],
      codeCcrca: [''],
      codeEntity: [''],
      numerozr: [''],
      codeSzr: [''],
      zoneCoron: [''],
      poucentage: [''],
      length: [''],
      area: [''],
      commune: [null]
    });
    this.load();
    this.loadCommunes();
  }

  get f() { return this.form.controls; }

  load(): void {
    this.loading = true;
    this.sharedService.url = this.listUrl;
    this.sharedService.getAll().subscribe({
      next: (data) => { this.items = data || []; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Erreur de chargement des quartiers.'; }
    });
  }

  loadCommunes(): void {
    this.sharedService.url = '/communes/s';
    this.sharedService.getAll().subscribe({
      next: (data) => { this.communes = data || []; },
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
    this.editingId = item.quartierId;
    this.form.patchValue({
      name: item.name, code: item.code, cav: item.cav, codeCav: item.codeCav,
      cCrca: item.cCrca, codeCcrca: item.codeCcrca, codeEntity: item.codeEntity,
      numerozr: item.numerozr, codeSzr: item.codeSzr, zoneCoron: item.zoneCoron,
      poucentage: item.poucentage, length: item.length, area: item.area,
      commune: item.commune ?? null
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
        this.message = this.editingId ? 'Quartier mis à jour.' : 'Quartier créé.';
        this.cancel();
        this.load();
      },
      error: () => { this.saving = false; this.error = 'Échec de l\'enregistrement.'; }
    });
  }

  remove(item: any): void {
    if (!confirm(`Supprimer le quartier « ${item.name} » ?`)) { return; }
    this.sharedService.url = this.resource;
    this.sharedService.delete(item.quartierId).subscribe({
      next: () => { this.message = 'Quartier supprimé.'; this.load(); },
      error: () => { this.error = 'Échec de la suppression.'; }
    });
  }
}
