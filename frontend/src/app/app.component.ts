import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SharedModule } from './shared/shared.module';

@Component({
    selector: 'app-root',
    imports: [
        CommonModule,
        RouterOutlet,
        RouterModule,
        //SharedModule,
    ],
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Sonaged sénégal';
  footerUrl = 'https://www.ganatan.com';
  footerLink = 'www.ganatan.com';

  constructor(private translateService: TranslateService) {
  }

  ngOnInit(): void {
    // Hors du constructeur : `use('fr')` déclenche une requête HTTP (chargement du fichier de
    // traduction) qui traverse les intercepteurs. Appelée depuis le constructeur du composant
    // racine, elle s'exécutait pendant la résolution DI du bootstrap (concurremment à celle des
    // gardes de route), et `ErrorInterceptor` y demandant `AuthService` retombait sur une
    // dépendance circulaire (`NG0200`) — `AuthService` pouvait être encore en cours de
    // construction ailleurs sur la même pile. `ngOnInit` s'exécute après la résolution DI du
    // composant, hors de cette fenêtre.
    this.translateService.setDefaultLang('fr');
    this.translateService.use('fr');
  }
}