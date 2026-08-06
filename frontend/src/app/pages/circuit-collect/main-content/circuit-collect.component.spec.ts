import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CircuitCollectComponent } from './circuit-collect.component';

describe('CircuitCollectComponent', () => {
  let component: CircuitCollectComponent;
  let fixture: ComponentFixture<CircuitCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CircuitCollectComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CircuitCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
