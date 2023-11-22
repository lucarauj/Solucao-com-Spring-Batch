package br.com.pagamento.web;

import br.com.pagamento.service.CNABService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("cnab")
public class CNABController {

    private final CNABService service;

    public CNABController(CNABService service) {
        this.service = service;
    }

    @PostMapping("upload")
    @CrossOrigin(origins = "http://localhost:9090")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        service.uploadCnabFile(file);
        return "Processamento iniciado";
    }
}
