import { Component, Input, ViewChild, ElementRef, EventEmitter, Output } from '@angular/core';
import { Papa } from 'ngx-papaparse';
import { ComponentService } from '../../../services/component.service'; 


@Component({
    selector: 'app-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.scss'],
    standalone: false
})
export class FileUploadComponent {

  @Input() multiple!: boolean;
  @Input() fileType!: string;
  @Input() dragDropEnabled = true;
  @Output() filesChanged!: EventEmitter<FileList>;
  @Input() label!: string;
  @Output() datas = this.getDataInCsvFile()
  @Input() data = this.getDataInCsvFile()
  @ViewChild('fileInput')
  inputRef!: ElementRef<HTMLInputElement>;

   uploadImg = "../asset/im"
   geometry = "../../assets/file/geometry.csv"
  constructor(
    private papa: Papa,
    private componentService: ComponentService
    ) {
    this.filesChanged = new EventEmitter();
  }

  addFiles(files: any): void {
    console.log(files);
    this.filesChanged.emit(files);
  }

  handleFileDrop(event: DragEvent) {
  
    this.handleFileSelect(event);
  }

  getDataInCsvFile(){
    return "datea infalr"
  }


  ConvertCSVtoJSON() {

    let csvData = '"Hello","World!"';
    this.papa.parse(csvData, {
      complete: (results) => {
        console.log('Parsed  : ', results.data[0][1]);
        // console.log(results.data.length);
      },
    });
  }

  handleFileSelect(evt: any) {
    var files = evt.target.files; // FileList object
    var file = files[0];
    var reader = new FileReader();
    reader.readAsText(file);
    reader.onload = (event: any) => {
      var csv = event.target.result; // Content of CSV file
      console.log(csv);
      this.papa.parse(csv, {
        skipEmptyLines: true,
        header: true,
        complete: (results) => {
          console.log('Parsed: k', results.data);
          this.componentService.outputFromDynamicComponent(results.data)
        },
      });
    };
  }

}
