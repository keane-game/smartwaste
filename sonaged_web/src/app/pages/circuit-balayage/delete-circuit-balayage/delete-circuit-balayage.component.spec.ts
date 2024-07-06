import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteCircuitBalayageComponent } from './delete-circuit-balayage.component';

describe('DeleteCircuitBalayageComponent', () => {
  let component: DeleteCircuitBalayageComponent;
  let fixture: ComponentFixture<DeleteCircuitBalayageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteCircuitBalayageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteCircuitBalayageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
