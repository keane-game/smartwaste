import { Component, OnInit } from '@angular/core';
import { headerTitleService } from '../services/headerTitle.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements  OnInit {

  constructor(private headerTitleService: headerTitleService){}
  ngOnInit() {
    this.headerTitleService.setTitle('Dashboard');
  }

}
