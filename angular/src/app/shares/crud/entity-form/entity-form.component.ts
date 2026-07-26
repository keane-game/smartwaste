import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EntityConfig, FieldConfig, findEntityConfig } from '../entity-config';
import { EntityCrudService } from '../entity-crud.service';

/**
 * Formulaire générique de création / modification d'une ressource backend.
 *
 * Le mode est déduit de la présence d'un paramètre `id` dans la route : sans `id` on crée
 * (POST), avec `id` on charge puis met à jour (PUT). Les contrôles et leurs validateurs sont
 * construits à partir de `EntityConfig.fields`, ce qui évite d'écrire un formulaire par entité.
 */
@Component({
  selector: 'app-entity-form',
  templateUrl: './entity-form.component.html',
  styleUrls: ['./entity-form.component.scss']
})
export class EntityFormComponent implements OnInit {

  config!: EntityConfig;
  form!: FormGroup;

  id: string | null = null;
  loading = false;
  saving = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private crud: EntityCrudService
  ) { }

  get isEdit(): boolean {
    return this.id !== null;
  }

  ngOnInit(): void {
    const key = this.route.snapshot.data['entityKey'];
    const config = findEntityConfig(key);
    if (!config) {
      this.error = `Ressource inconnue : ${key}`;
      return;
    }
    this.config = config;
    this.id = this.route.snapshot.paramMap.get('id');
    this.buildForm();

    if (this.isEdit) {
      this.loadExisting();
    }
  }

  private buildForm(): void {
    const controls: Record<string, any> = {};
    for (const field of this.config.fields) {
      const validators = [];
      if (field.required) { validators.push(Validators.required); }
      if (field.maxLength) { validators.push(Validators.maxLength(field.maxLength)); }
      controls[field.name] = [field.type === 'checkbox' ? false : null, validators];
    }
    this.form = this.fb.group(controls);
  }

  private loadExisting(): void {
    this.loading = true;
    this.crud.getById(this.config, this.id!).subscribe({
      next: entity => {
        // patchValue ignore les clés absentes du formulaire : les champs non éditables
        // (relations, géométrie) du DTO backend sont donc simplement laissés de côté.
        this.form.patchValue(entity ?? {});
        this.loading = false;
      },
      error: err => { this.error = err.message; this.loading = false; }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      // Sans cela, les messages d'erreur n'apparaissent pas tant que l'utilisateur
      // n'a pas visité chaque champ.
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.error = '';

    const payload = { ...this.form.value };
    if (this.isEdit) {
      payload[this.config.idField] = isNaN(Number(this.id)) ? this.id : Number(this.id);
    }

    const request$ = this.isEdit
      ? this.crud.update(this.config, this.id!, payload)
      : this.crud.create(this.config, payload);

    request$.subscribe({
      next: () => { this.saving = false; this.back(); },
      error: err => { this.error = err.message; this.saving = false; }
    });
  }

  back(): void {
    // Remonte à la liste : ../.. en édition (/:id/edit), .. en création (/create).
    this.router.navigate([this.isEdit ? '../..' : '..'], { relativeTo: this.route });
  }

  /** Message de validation à afficher sous un champ, ou chaîne vide. */
  errorFor(field: FieldConfig): string {
    const control = this.form.get(field.name);
    if (!control || !control.touched || control.valid) { return ''; }
    if (control.hasError('required')) { return `${field.label} est obligatoire.`; }
    if (control.hasError('maxlength')) { return `${field.label} dépasse ${field.maxLength} caractères.`; }
    return 'Valeur invalide.';
  }
}
