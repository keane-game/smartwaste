import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteDepotoirComponent } from './delete-depotoir.component';

describe('DeleteDepotoirComponent', () => {
  let component: DeleteDepotoirComponent;
  let fixture: ComponentFixture<DeleteDepotoirComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteDepotoirComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteDepotoirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
