import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteQuartierComponent } from './delete-quartier.component';

describe('DeleteQuartierComponent', () => {
  let component: DeleteQuartierComponent;
  let fixture: ComponentFixture<DeleteQuartierComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteQuartierComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DeleteQuartierComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
