import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateQuartierComponent } from './create-quartier.component';

describe('CreateQuartierComponent', () => {
  let component: CreateQuartierComponent;
  let fixture: ComponentFixture<CreateQuartierComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateQuartierComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateQuartierComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
