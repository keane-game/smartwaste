import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCircuitComponent } from './create-circuit.component';

describe('CreateCircuitComponent', () => {
  let component: CreateCircuitComponent;
  let fixture: ComponentFixture<CreateCircuitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCircuitComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateCircuitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
