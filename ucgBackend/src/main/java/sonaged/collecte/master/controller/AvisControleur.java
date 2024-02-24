package sonaged.collecte.master.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sonaged.collecte.master.model.Avis;
import sonaged.collecte.master.service.AvisService;

@AllArgsConstructor
@RequestMapping("avis")
@RestController
public class AvisControleur {
    private final AvisService avisService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void creer(@RequestBody Avis avis) {
        this.avisService.creer(avis);
    }
}
