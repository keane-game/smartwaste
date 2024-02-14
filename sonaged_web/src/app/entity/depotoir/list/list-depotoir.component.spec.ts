import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListDepotoirComponent } from './list-depotoir.component';

describe('ListDepotoirComponent', () => {
  let component: ListDepotoirComponent;
  let fixture: ComponentFixture<ListDepotoirComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListDepotoirComponent]
    });
    fixture = TestBed.createComponent(ListDepotoirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
