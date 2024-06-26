import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteCircuitCollectComponent } from './delete-circuit-collect.component';

describe('DeleteCircuitCollectComponent', () => {
  let component: DeleteCircuitCollectComponent;
  let fixture: ComponentFixture<DeleteCircuitCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteCircuitCollectComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteCircuitCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
