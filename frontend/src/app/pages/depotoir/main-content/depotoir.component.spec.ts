import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepotoirComponent } from './depotoir.component';

describe('DepotoirComponent', () => {
  let component: DepotoirComponent;
  let fixture: ComponentFixture<DepotoirComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DepotoirComponent]
    });
    fixture = TestBed.createComponent(DepotoirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
