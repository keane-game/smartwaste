import { Component, OnInit, ViewChild } from '@angular/core';
import { headerTitleService } from '../../services/headerTitle.service';
import  { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {


  canvas: any;
  ctx: any;
  @ViewChild('mychart') mychart: any;
  
  urlProfile : String = "../../assets/images/user.png";
  iconCommune : String = "../../assets/images/commune.png";
  iconPeople: String = "../../assets/images/people.png";
  iconBenne: String = "../../assets/images/bennes.png";
  iconBacs : String = "../../assets/images/bacs1.png"
  iconPrn : String = "../../assets/images/prn.png";
  iconCircuit : String = "../../assets/images/circuit.png"
  
  constructor(private headerTitleService: headerTitleService){}
  ngOnInit() {
    this.headerTitleService.setTitle('Dashboard');
  }


  data = {
    labels: ['M1', 'M2', 'M3', 'M4', 'M5', 'M6'],
    datasets: [{
      label : "CNSA-FR",
      data : [20, 10, 16, 27, 150, 97],
      tension : 0.3,
      pointStyle : false,
      borderWidth : 1.5,
    }
    ],
  };
  

  ngAfterViewInit(){
    this.canvas = this.mychart.nativeElement; 
    this.ctx = this.canvas.getContext('2d');
    new Chart(this.ctx, {
      type: 'line',
      data : {
        labels: ['M1', 'M2', 'M3', 'M4', 'M5', 'M6'],
        datasets: [{
          label : "CNSA-FR",
          data : [20, 10, 16, 27, 150, 97],
          tension : 0.3,
          pointStyle : false,
          borderWidth : 1.5,
        },
        {
          label : "CNSA-PPA",
          data : [15, 30, 69, 13, 15, 130],
          tension : 0.3,
          pointStyle : false,
          borderWidth : 1.5
        },
        {
          label : "GAR-RENATER",
          data : [84, 30, 10, 15, 70, 117],
          tension : 0.3,
          pointStyle : false,
          borderWidth : 1.5
        }
        ],
        
      },
       options: {
        maintainAspectRatio : false,
           plugins: {
            title: {
                display: true,
                text: 'Productivité sur 6 sprints',
                color : 'black',
                
            },
            legend : {
              
              position : 'bottom',
              // align : 'start',
              labels : {
                boxWidth : 30,
                boxHeight : 1,
                font : {
                  size : 9
                },
              }
              
            }
        }
    }
    
    }
    ) 
  }

}
