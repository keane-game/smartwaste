import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

@Injectable({
    providedIn: 'root'
})
export class ModalService {

    dialogRef!: MatDialogRef<any>
    constructor(private matDialog: MatDialog) { }


    openModal<T>(
        component: any,
        data: any = {},
        config: any = {}
    ): MatDialogRef<T> {
        const defaultConfig = {
            disableClose: true,
            panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
            maxHeight: '100vh',
            maxWidth: '100%',
            data: data
        };

        const mergedConfig = { ...defaultConfig, ...config };

        const dialogRef = this.matDialog.open<T>(component, mergedConfig);

        // Les CreateXxxComponent (Commune, Quartier, Departement...) lisent `id`/`currentXxx`
        // comme des proprietes directes du composant, pas via MAT_DIALOG_DATA (seul
        // `DeleteComponent` fait ca). Cet assign etait auparavant une methode `setDataInstance`
        // jamais appelee (`//this.setDataInstance(...)`, dead code) avec en plus un nom de champ
        // en dur (`currentQuartier` uniquement) — resultat : "Modifier" ne pre-remplissait jamais
        // le formulaire et `id` restait `undefined`, donc chaque edition creait un doublon au lieu
        // de mettre a jour l'existant. Trouve en verifiant le flux CRUD en conditions reelles.
        Object.assign(dialogRef.componentInstance as any, data);

        return dialogRef;
    }

    closeAllDialogs(): void {
        this.matDialog.closeAll();
    }

    openDynamicModal(
        component: any,
        data: any = {},
        defaultConfig?: {   panelClass: ['md:w-5/5', 'w-full', 'full-with-dialog'],
        disableClose:  true,
        maxHeight:  '100vh',
        maxWidth:  '100%'}
    ): MatDialogRef<any> {

        const config = {...defaultConfig,
            data
        };

        return this.matDialog.open(component, config);
    }
}
