import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SharedModule } from './shared/shared.module';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterModule,
    //SharedModule, 
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Sonaged sénégal';
  footerUrl = 'https://www.ganatan.com';
  footerLink = 'www.ganatan.com';

  constructor(private translateService: TranslateService) {
    translateService.setDefaultLang('fr');
    translateService.use('fr');
  }
}