import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatListModule } from '@angular/material/list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatSidenavModule } from '@angular/material/sidenav'
import { MatCheckboxModule } from "@angular/material/checkbox";

import { NgMultiSelectDropDownModule } from 'ng-multiselect-dropdown'
import { NgChartsModule } from 'ng2-charts';
// import { A11yModule } from '@angular/cdk/a11y';
// import { DragDropModule } from '@angular/cdk/drag-drop';
// import { PortalModule } from '@angular/cdk/portal';
// import { ScrollingModule } from '@angular/cdk/scrolling';
// import { CdkStepperModule } from '@angular/cdk/stepper';
// import { CdkTableModule } from '@angular/cdk/table';
// import { CdkTreeModule } from '@angular/cdk/tree';
// import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
// import { MatAutocompleteModule } from '@angular/material/autocomplete';
// import { MatBadgeModule } from '@angular/material/badge';
// import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
// import { MatButtonToggleModule } from '@angular/material/button-toggle';
// import { MatCheckboxModule } from '@angular/material/checkbox';
// import { MatChipsModule } from '@angular/material/chips';
// import { MatDatepickerModule } from '@angular/material/datepicker';
// import { MatDividerModule } from '@angular/material/divider';
// import { MatExpansionModule } from '@angular/material/expansion';
// import { MatGridListModule } from '@angular/material/grid-list';
// import { MatMenuModule } from '@angular/material/menu';
// import { MatNativeDateModule } from '@angular/material/core';
// import { MatProgressBarModule } from '@angular/material/progress-bar';
// import { MatRadioModule } from '@angular/material/radio';
// import { MatRippleModule } from '@angular/material/core';
// import { MatSelectModule } from '@angular/material/select';
// import { MatSidenavModule } from '@angular/material/sidenav';
// import { MatSliderModule } from '@angular/material/slider';
// import { MatSlideToggleModule } from '@angular/material/slide-toggle';
// import { MatSnackBarModule } from '@angular/material/snack-bar';
// import { MatStepperModule } from '@angular/material/stepper';
// import { MatTabsModule } from '@angular/material/tabs';
// import { MatToolbarModule } from '@angular/material/toolbar';
// import { MatTooltipModule } from '@angular/material/tooltip';
// import { MatTreeModule } from '@angular/material/tree';

export const MAT_MATERIAL = [
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatIconModule,
        MatInputModule,
        MatListModule,
        MatPaginatorModule,
        MatSortModule,
        MatTableModule,
        MatFormFieldModule,
        MatSelectModule,
        MatDividerModule,
        MatListModule,
        MatSidenavModule,
        MatCheckboxModule,
        NgMultiSelectDropDownModule,
        NgChartsModule
    ];

 @NgModule({
        exports: [MAT_MATERIAL],
        imports: [MAT_MATERIAL]
})

export class MaterialsModule {
}

