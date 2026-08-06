import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EsriComponent } from './esri.component';

describe('EsriComponent', () => {
  let component: EsriComponent;
  let fixture: ComponentFixture<EsriComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EsriComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EsriComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
