import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaginationCustumerComponent } from './pagination-custumer.component';

describe('PaginationCustumerComponent', () => {
  let component: PaginationCustumerComponent;
  let fixture: ComponentFixture<PaginationCustumerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaginationCustumerComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PaginationCustumerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
