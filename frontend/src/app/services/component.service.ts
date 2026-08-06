

import { Injectable, Type } from '@angular/core';
import { FileUploadComponent } from '../shared/components/file-upload/file-upload.component';
import { Subject } from 'rxjs';


@Injectable({ providedIn: 'root' })
export class ComponentService {

  private output = new Subject<any>();

  component!: {component: Type<any>, inputs: Record<string, unknown>}

  constructor() {}

  getInputComponents(label: any) {
    // this.component.component = FileUploadComponent
    // this.component.inputs = { name: 'Dr. IQ', bio: 'Smart as they come' }
    this.component = {component:FileUploadComponent, inputs: { label: label, data: "" }}
    return this.component;
  }

  getNgMultiselectDropdown(){

  }

  getObservable() {
    return this.output.asObservable();
  }

  outputFromDynamicComponent(data: any) {
    this.output.next(data);
  }


}