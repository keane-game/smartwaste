package sonaged.collecte.master.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

    String uploadDataDepartment(MultipartFile file);
    String uploadDataCommune(MultipartFile file);
    String uploadDataQuartier(MultipartFile file);
    String uploadDataCircuitCollect(MultipartFile file);
    String uploadDataCircuitBalayage(MultipartFile file);
    String uploadDataDepotoir(MultipartFile file);


}
