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

        //this.setDataInstance(data, dialogRef);

        return dialogRef;
    }

    closeAllDialogs(): void {
        this.matDialog.closeAll();
    }

    setDataInstance(data: any, dialogRef: any) {
        if (data.id) {
            (dialogRef.componentInstance as any).id = data.id;
        }
        if (data.currentQuartier) {
            (dialogRef.componentInstance as any).currentQuartier = data.currentQuartier;
        }
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
