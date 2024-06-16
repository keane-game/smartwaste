package sonaged.collecte.master.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sonaged.collecte.master.service.DashboardService;
import sonaged.collecte.master.service.UploadFileService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

   // private final DashboardService dashboardService;
    private final UploadFileService uploadFileService;

    @PostMapping(value = "/commune" , consumes = "multipart/form-data")
    public String uploadDataCommune(@RequestParam("file") MultipartFile file) {
        return uploadFileService.uploadDataCommune (file);
    }

    @PostMapping(value = "/department" , consumes = "multipart/form-data")
    public String uploadDataDepartment(@RequestParam("file") MultipartFile file) {
       return uploadFileService.uploadDataDepartment (file);
    }
}


