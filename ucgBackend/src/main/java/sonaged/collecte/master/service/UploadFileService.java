package sonaged.collecte.master.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface UploadFileService {

    String uploadDataDepartment(MultipartFile file);
    String uploadDataCommune(MultipartFile file);
    String uploadDataQuartier(MultipartFile file);
    String uploadDataCircuit(MultipartFile file);
    String uploadDataDepotoir(MultipartFile file);


}
