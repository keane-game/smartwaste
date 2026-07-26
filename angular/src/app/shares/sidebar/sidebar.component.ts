import { Component, ElementRef, Renderer2, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
declare var $: any;

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {

  //@ViewChild('sidebar') elRefs: ElementRef;

  constructor(
    private elRef: ElementRef,
    private renderer: Renderer2,
    private authService: AuthService,
    private router: Router
  ) { }

  /** Déconnexion : purge le jeton stocké puis retourne à l'écran de login. */
  logout(event: Event): void {
    event.preventDefault();
    this.authService.logout();
    this.router.navigate(['/login']);
  }




  jquery(event: any): void{
    const el = this.elRef.nativeElement.querySelector('#sidebar');

    this.renderer.addClass(el, 'active');

    $('#sidebarCollapse').on('click', () => {
        $('#sidebar').toggleClass('active');
    });

  }
}
