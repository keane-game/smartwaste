import { Component, ChangeDetectorRef, EventEmitter, Input, AfterContentChecked, Output, Renderer2, ViewChild } from '@angular/core';
import { headerTitleService } from '../../../services/headerTitle.service';
declare var $: any;

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss'],
    standalone: false
})

export class HeaderComponent implements AfterContentChecked {


  isClicked = false;
  pageTitle = "Dashboard"
  userName!: string;

  urlProfile : String = "../../assets/images/user.png";
  iconInfo1 : String = "../../assets/images/icon-notification.png";


  @Output() childEvent = new EventEmitter<any>();
  constructor(
    private headerTitleService: headerTitleService,
    private changeDetector: ChangeDetectorRef,

  ) { }

  triggerParentFunction(): void {
    this.childEvent.emit();
  }

  ngOnInit(): void {
    this.getUserName();
  }

  getUserName(): void {
    // if (this.keycloakService.isLoggedIn()) {
    //   const userProfile = this.keycloakService.getUserProfile().then((data: any) => {
    //     this.userName = data.username;
    //     console.log(data);
    //   });
    // } else {
    //   this.userName = 'Not logged in';
    // }
  }


  ngAfterContentChecked(): void {
    this.changeDetector.detectChanges();
  }

  ngAfterViewInit(): void {
    Promise.resolve().then(()=> {
      this.headerTitleService.title.subscribe(updatedTitle => {
        this.pageTitle = updatedTitle;
      });
    })
  }

}
  


