import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CircuitBalayageComponent } from './circuit-balayage.component';

describe('CircuitBalayageComponent', () => {
  let component: CircuitBalayageComponent;
  let fixture: ComponentFixture<CircuitBalayageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CircuitBalayageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CircuitBalayageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
