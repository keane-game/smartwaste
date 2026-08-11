# DESIGN_SYSTEM.md — Smart Collect (refonte 2026-08-09)

> Phase 1 de la refonte. Généré via le skill `ui-ux-pro-max` (`--design-system`, `--domain color`,
> `--domain icons`, `--stack angular`), puis arbitré manuellement sur un point : la recherche de
> palette par défaut pour « smart city / IoT / civic » revient systématiquement sur une base
> **bleue** (Government Portal / Civic Services : `#1E40AF`). Une décision utilisateur antérieure,
> explicite et déjà validée, écarte le bleu (« reste sur la couleur vert pas de bleu », 2026-08-07).
> Ce document **conserve la palette verte déjà en production** (`Agriculture/Farm Tech`, vérifiée
> WCAG) comme identité de marque, et l'étend avec un mode sombre pour porter l'ambition « Smart City
> Premium » sans réintroduire de bleu.

## 1. Couleurs

### Mode clair — identité de marque (inchangée, déjà en production, ne pas re-choisir)
| Token | Valeur | Usage |
|---|---|---|
| `--color-primary` | `#15803D` | Actions principales, liens actifs, focus |
| `--color-primary-dark` | `#14532D` | Hover primary, texte sur fond clair |
| `--color-primary-light` | `#22C55E` | Accents secondaires, indicateurs positifs |
| `--color-accent` | `#A16207` | CTA secondaire, mise en avant (or-récolte) |
| `--color-background` | `#F0FDF4` | Fond de page |
| `--color-surface` | `#FFFFFF` | Cards, panneaux |
| `--color-foreground` | `#14532D` | Titres |
| `--color-text` | `#334155` | Corps de texte |
| `--color-muted` | `#64748B` | Texte secondaire, légendes |
| `--color-border` | `#BBF7D0` | Bordures, séparateurs |
| `--color-destructive` | `#DC2626` | Erreurs, suppression, alertes critiques |
| `--color-warning` | `#D97706` | Avertissements |
| `--color-success` | `#15803D` | Confirmations (= primary, cohérence sémantique) |

### Mode sombre — nouveau, pour les écrans opérationnels (dashboard, IoT, carte)
Le brief « Smart City Premium » (IoT, données, opérations) appelle un registre plus technique que
les écrans citoyens. Plutôt qu'emprunter au bleu (rejeté), le mode sombre s'appuie sur un
**graphite neutre** (`slate`, sans teinte bleutée dominante) avec le même vert comme unique accent
de marque — cohérence de marque garantie entre les deux modes.
| Token | Valeur | Usage |
|---|---|---|
| `--color-background` (dark) | `#0F1712` | Fond de page (vert-graphite très sombre, pas de bleu-nuit) |
| `--color-surface` (dark) | `#16211B` | Cards, panneaux |
| `--color-primary` (dark) | `#22C55E` | Plus clair qu'en mode clair — contraste sur fond sombre |
| `--color-foreground` (dark) | `#F0FDF4` | Titres |
| `--color-text` (dark) | `#CBD5C8` | Corps de texte |
| `--color-muted` (dark) | `#7C9384` | Texte secondaire |
| `--color-border` (dark) | `#243B2E` | Bordures |
| `--color-destructive` (dark) | `#F87171` | Erreurs (plus clair que le mode jour, lisible sur fond sombre) |

**Usage recommandé** : mode clair par défaut partout (citoyen, agent, formulaires admin) ; mode
sombre en **option** sur le dashboard et les écrans de supervision temps réel (IoT, carte, centre
d'alertes) où un fond sombre réduit la fatigue visuelle sur de longues sessions de monitoring —
jamais imposé, toujours un choix utilisateur mémorisé (`localStorage`), jamais la valeur par défaut
d'un compte fraîchement créé.

### Couleurs sémantiques (état métier, pas juste UI)
| Sens | Couleur | Où |
|---|---|---|
| Débordement / critique | `--color-destructive` (`#DC2626`) | Priorité `DEBORDEMENT`, alerte `DANGER` |
| État inconnu | `--color-muted` (`#64748B`) — délibérément neutre, pas rouge | Priorité `ETAT_INCONNU` : ne pas sur-alarmer une simple absence de donnée |
| À surveiller | `--color-warning` (`#D97706`) | Priorité `A_SURVEILLER`, alerte `WARNING` |
| Rien à faire / résolu | `--color-success` | Priorité `RIEN_A_FAIRE`, alerte résolue |
| Capteur actif | `--color-primary-light` | Santé capteur |
| Capteur muet | `--color-warning` | Santé capteur — pas destructive : un silence n'est pas encore une panne confirmée |

## 2. Typographie (inchangée, déjà choisie pour l'accessibilité et le contexte gouvernemental)
- **Titres** : Lexend — conçue pour la lisibilité, recommandée en contexte scolaire/institutionnel.
- **Corps** : Source Sans 3.

| Niveau | Taille | Poids | Usage |
|---|---|---|---|
| Display | 2.5rem / 40px | 700 | Chiffres clés (KPI dashboard) |
| H1 | 1.75rem / 28px | 700 | Titre de page |
| H2 | 1.375rem / 22px | 600 | Titre de section |
| H3 | 1.125rem / 18px | 600 | Titre de card |
| Body | 1rem / 16px | 400 | Corps de texte — jamais en-dessous |
| Small | 0.875rem / 14px | 400 | Tableaux, corps secondaire |
| Caption | 0.75rem / 12px | 500 | Légendes, métadonnées — taille plancher, jamais plus petit |

Ligne : 1.5 pour le corps, 1.25 pour les titres. Contenu français : accepter les mots longs
(césure activée sur mobile, `hyphens: auto`).

## 3. Espacement, rayon, ombres (existants, conservés)
Échelle `--space-xs` (4px) → `--space-3xl` (64px), déjà posée et cohérente. `--radius: 0.75em`
(cards/boutons), `--radius-sm: 6px` (badges/inputs). Ombres à deux couches teintées vert
(`--shadow-sm/md/lg`) — garder la teinte de marque plutôt que des ombres neutres grises.

## 4. Icônes
**Lucide Angular** (imposé par le cahier des charges, section 4) — famille outline, cohérente,
24px par défaut (20px en contexte dense type tableau). Ne jamais mélanger avec les icônes Bootstrap
Icons (`ri-*`) actuellement utilisées dans `sidebar.component.html` — migration complète, pas de
mélange des deux familles qui casserait la cohérence visuelle recherchée.

## 5. Composants

### Boutons
| Variante | Usage | Style |
|---|---|---|
| `primary` | Action principale d'un écran | Fond `--color-primary`, texte blanc |
| `secondary` | Action alternative | Bordure `--color-primary`, fond transparent, texte `--color-primary` |
| `ghost` | Action tertiaire, dans un tableau | Pas de fond ni bordure, hover `--color-muted-surface` |
| `danger` | Suppression, désactivation | Fond `--color-destructive` |
| `icon` | Action compacte (tableau, toolbar) | 40×40px minimum (accessibilité tactile) |

Tous : hauteur minimale 44px (cible tactile), transition 150-200ms, `cursor: pointer`, état
`:focus-visible` avec `--focus-ring`, état `:disabled` à 40% d'opacité + `cursor: not-allowed`.

### Inputs
États obligatoires : `default`, `:focus` (ring vert), `.is-invalid` (bordure + texte
`--color-destructive`, message d'erreur **sous le champ**, jamais uniquement en haut de formulaire),
`:disabled` (fond `--color-muted-surface`). Label toujours visible (jamais placeholder-only).

### Tables
Header : fond `--color-muted-surface`, texte `--color-muted` en majuscules, `text-sm`. Ligne :
hover `--color-background`, ligne sélectionnée `--color-primary-light` à 10% d'opacité. Pagination
en pied de tableau. **État vide obligatoire** : icône + message contextuel + action (« Aucun point
de collecte dans cette commune — importer le référentiel »), jamais un tableau simplement vide.

### Cards
Trois variantes : `card` (contenu standard), `kpi-card` (chiffre clé + icône + tendance,
dashboard), `alert-card` (bordure gauche colorée selon gravité, utilisée dans le centre d'alertes).

### Badges
Un par état métier (voir couleurs sémantiques §1) — `DEBORDEMENT`/`ETAT_INCONNU`/`A_SURVEILLER`/
`RIEN_A_FAIRE` côté tournées ; `SIGNALE`/`EN_COURS`/`TRAITE`/`REJETE` côté signalements citoyens ;
actif/silencieux/désactivé côté capteurs.

## 6. Animation
Niveau **Standard** (motion 6/10) : transitions 200-300ms, `ease-out` pour l'entrée, plus rapide en
sortie (150ms) que l'entrée — cf. `references/quick-reference.md` §7 du skill. Le temps réel (SSE)
doit s'animer pour se faire remarquer sans être intrusif : un nouvel élément dans le flux d'activité
entre par un léger slide + fade (250ms), une carte qui change d'état (alerte levée) pulse une fois
brièvement plutôt que de changer de couleur instantanément. `prefers-reduced-motion` respecté
partout — désactive stagger/slide, garde uniquement les transitions de couleur/opacité.

## 7. Accessibilité
Contraste 4.5:1 minimum vérifié sur les deux modes (clair/sombre) — le vert primary `#15803D` sur
fond blanc et `#22C55E` sur fond `#0F1712` sont tous deux conformes AA. Navigation clavier complète,
focus visible partout (jamais `outline: none` sans remplacement), `aria-label` sur tout bouton
icône-seul, tailles de cible tactile 44×44px minimum (section 2 du barème skill, priorité CRITICAL).

## 8. Implémentation Angular (stack cible)
- **Standalone partout**, `provideRouter()`/`provideHttpClient()` dans `app.config.ts` — pas de
  nouveau `NgModule`.
- **`ChangeDetectionStrategy.OnPush`** sur tous les nouveaux composants, combiné aux **Signals**
  pour l'état local — élimine la plupart des cycles de détection inutiles sur des écrans à fort
  volume de données (tableaux, carte).
- **Reactive Forms** exclusivement (pas de `ngModel`), `updateOn: 'blur'` sur les champs à
  validation coûteuse (ex. vérification d'unicité email).
- Tailwind CSS remplace Bootstrap — les tokens ci-dessus deviennent la config `tailwind.config.ts`
  (`theme.extend.colors`), pas des classes utilitaires ad hoc.

## 9. À faire respecter dans chaque nouvel écran (checklist de livraison)
- [ ] Pas d'emoji comme icône (Lucide uniquement)
- [ ] `cursor: pointer` sur tout élément cliquable
- [ ] États hover avec transition 150-300ms
- [ ] Contraste texte 4.5:1 minimum (light **et** dark)
- [ ] Focus clavier visible
- [ ] `prefers-reduced-motion` respecté
- [ ] Responsive testé à 375px, 768px, 1024px, 1440px
- [ ] États loading (skeleton)/empty/error/success gérés
