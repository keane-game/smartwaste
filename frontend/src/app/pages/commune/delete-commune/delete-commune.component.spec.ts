import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteCommuneComponent } from './delete-commune.component';

describe('DeleteCommuneComponent', () => {
  let component: DeleteCommuneComponent;
  let fixture: ComponentFixture<DeleteCommuneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteCommuneComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteCommuneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
