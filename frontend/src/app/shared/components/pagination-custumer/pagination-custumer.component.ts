import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

@Component({
  selector: 'app-pagination-custumer',
  templateUrl: './pagination-custumer.component.html',
  styleUrls: ['./pagination-custumer.component.scss']
})
export class PaginationCustumerComponent implements OnInit {
  @Input() length: number = 0;
  @Input() pageSize: number = 10;
  @Input() pageSizeOptions: number[] = [5, 10, 20];
  @Input() totalPages: number = 0;
  @Output() page = new EventEmitter<any>();
  @Output() pageChange = new EventEmitter<{ pageIndex: number, pageSize: number }>();

  currentPage: number = 1;

  ngOnInit() {
    this.updatePageData();
   
  }

  calculateTotalPages() {
    console.log(this.length)
    if(this.length != 0)
    this.totalPages = Math.ceil(this.length / this.pageSize);
  }

  updatePageData() {
    this.page.emit({
      pageIndex: this.currentPage,
      pageSize: this.pageSize,
      length: this.length,
      totalPages: this.totalPages
    });
    console.log(  this.totalPages);
  }
  private emitPageChange() {
    this.pageChange.emit({
      pageIndex: this.currentPage - 1,
      pageSize: this.pageSize
    });
  }

  setPageSize(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.pageSize = +target.value;
    this.currentPage = 1; // Reset to first page$$
    this.emitPageChange();
  }

  nextPage() {
    if (this.currentPage < this.totalPages ) {
      this.currentPage++;
      this.emitPageChange();
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.emitPageChange();
    }
  }

  //   previousPage(): void {
//     if (this.p > 1) {
//       this.p--;
//       this.updateDataSource();
//       this.updatePageData()
//     }
//   }
  
//   nextPage(): void {
//     if (this.p < this.totals / this.itemsPerPage) {
//       this.p++;
//       this.updateDataSource();
//       this.updatePageData();
//     }
//   }

  getNumberPage(totals : number,itemsPerPage :number):number {
     return Math.ceil(totals / itemsPerPage);
    
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.emitPageChange();
  }

  getPages(currentPage: number): number[] {

    if (this.length != 0) {
      const totalPages = Math.ceil(this.length / this.pageSize);
      if (totalPages <= 3) {
        return Array(totalPages).fill(0).map((_, i) => i + 1);
      } else if (currentPage === 1) {
        return [1, 2, 3];
      } else if (currentPage === this.totalPages) {
        return [currentPage - 2, currentPage - 1, currentPage];
      } else {
        return [currentPage - 1, currentPage, currentPage + 1];
      }
    } else {
      return []
    }
  
  }
}
















// import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
// import { MatTableDataSource } from '@angular/material/table';
// import { MatPaginator } from '@angular/material/paginator';
// import { MatSort } from '@angular/material/sort';

// @Component({
//   selector: 'app-pagination-custumer',
//   templateUrl: './pagination-custumer.component.html',
//   styleUrl: './pagination-custumer.component.scss'
// })
// export class PaginationCustumerComponent implements AfterViewInit, OnInit {
//   @Input() itemsPerPage: number = 10;
//   @Input() dataSource: MatTableDataSource<any> = new MatTableDataSource<any>();
//   pageNumber: number = 1;
//   @Input() data: any[] = [];
//   @Input() totals: number = 0; 
//   @ViewChild(MatPaginator) paginator!: MatPaginator;
//   @ViewChild(MatSort) sort!: MatSort; 
//   @Output() page = new EventEmitter<any>();
//   p: number = 1;
//   isNextPage = false;
//   isAfterPage = false;
//   currentPage: any;
//   pageSize: any;
//   length: any;
//   totalPages: any;
  

//   ngOnInit(): void {
//     this.dataSource.paginator = this.paginator; 
//     this.dataSource.sort = this.sort;
//     this.isNextPage = this.p < this.totals / this.itemsPerPage
//     this.updateDataSource()
//   }

//   getNumberPage(totals : number,itemsPerPage :number):number {
//     return Math.round(totals/itemsPerPage);

//   }
//   ngAfterViewInit(): void {
//     this.dataSource.paginator = this.paginator;
//     this.dataSource.sort = this.sort;
//   }

//   updateItemsPerPage(event: any): void {
//     this.itemsPerPage = event.target.value;
//     this.updateDataSource();
//   }

//   getPages(currentPage: number): number[] {

//     if (this.data != undefined) {
//       const totalPages = Math.ceil(this.totals / this.itemsPerPage);
//       if (totalPages <= 3) {
//         return Array(totalPages).fill(0).map((_, i) => i + 1);
//       } else if (currentPage === 1) {
//         return [1, 2, 3];
//       } else if (currentPage === totalPages) {
//         return [currentPage - 2, currentPage - 1, currentPage];
//       } else {
//         return [currentPage - 1, currentPage, currentPage + 1];
//       }
//     } else {
//       return []
//     }
  
//   }

//   goToPage(page: number): void {
//     this.p = page;
//     this.updateDataSource();
//     this. updatePageData();

//   }


//   updateDataSource(): void {
    
//     const startIndex = (this.p - 1) * this.itemsPerPage;
//     const endIndex = Math.min(startIndex + this.itemsPerPage, this.totals);
//     this.dataSource.data = this.data.slice(startIndex, endIndex);
//   }
//   updatePageData() {
//     this.page.emit({
//       pageIndex: (this.p - 1) * this.itemsPerPage,
//       pageSize: this.pageSize,
//       length: this.totals,
//       totalPages: this.totalPages
//     });
//   }

//   previousPage(): void {
//     if (this.p > 1) {
//       this.p--;
//       this.updateDataSource();
//       this.updatePageData()
//     }
//   }
  
//   nextPage(): void {
//     if (this.p < this.totals / this.itemsPerPage) {
//       this.p++;
//       this.updateDataSource();
//       this.updatePageData();
//     }
//   }
// }

