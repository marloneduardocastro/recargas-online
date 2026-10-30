package co.com.recargas.web;

import co.com.recargas.dto.OperadorDto;
import co.com.recargas.dto.VendedorDto;
import co.com.recargas.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/operadores")
    public List<OperadorDto> operadores() {
        return catalogoService.operadoresActivos();
    }

    @GetMapping("/vendedores")
    public List<VendedorDto> vendedores() {
        return catalogoService.vendedoresActivos();
    }
}
