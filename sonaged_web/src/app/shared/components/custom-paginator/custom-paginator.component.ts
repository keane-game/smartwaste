import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';

@Component({
  selector: 'app-custom-paginator',
  templateUrl: './custom-paginator.component.html',
  styleUrls: ['./custom-paginator.component.scss']
})
export class CustomPaginatorComponent implements OnInit {
  @Input() length: number = 0;
  @Input() pageSize: number = 10;
  @Input() pageSizeOptions: number[] = [5, 10, 20];
  @Output() page = new EventEmitter<any>();

  currentPage: number = 0;
  totalPages: number = 0;

  ngOnInit() {
    this.calculateTotalPages();
    this.updatePageData();
  }

  calculateTotalPages() {
    if(this.length != undefined && this.pageSize)
    this.totalPages = Math.ceil(this.length / this.pageSize);
  }

  updatePageData() {
    this.page.emit({
      pageIndex: this.currentPage,
      pageSize: this.pageSize,
      length: this.length,
      totalPages: this.totalPages
    });
  }

  setPageSize(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.pageSize = +target.value;
    this.currentPage = 0; // Reset to first page
    this.calculateTotalPages();
    this.updatePageData();
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.updatePageData();
    }
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.updatePageData();
    }
  }
}
