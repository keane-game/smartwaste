import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCircuitCollectComponent } from './create-circuit-collect.component';

describe('CreateCircuitCollectComponent', () => {
  let component: CreateCircuitCollectComponent;
  let fixture: ComponentFixture<CreateCircuitCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCircuitCollectComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateCircuitCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
