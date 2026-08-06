import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotifcationComponent } from './notifcation.component';

describe('NotifcationComponent', () => {
  let component: NotifcationComponent;
  let fixture: ComponentFixture<NotifcationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotifcationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NotifcationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
