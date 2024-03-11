import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateDepotoirComponent } from './create-depotoir.component';

describe('CreateDepotoirComponent', () => {
  let component: CreateDepotoirComponent;
  let fixture: ComponentFixture<CreateDepotoirComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CreateDepotoirComponent]
    });
    fixture = TestBed.createComponent(CreateDepotoirComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
