import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCircuitBalayageComponent } from './create-circuit-balayage.component';

describe('CreateCircuitBalayageComponent', () => {
  let component: CreateCircuitBalayageComponent;
  let fixture: ComponentFixture<CreateCircuitBalayageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCircuitBalayageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateCircuitBalayageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
