//headerTitle.service.ts

import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export  class  headerTitleService {
   title = new BehaviorSubject('');

  setTitle(title: string) {
    this.title.next(title);
  }
}


