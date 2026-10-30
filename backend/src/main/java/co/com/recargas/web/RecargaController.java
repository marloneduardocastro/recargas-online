package co.com.recargas.web;

import co.com.recargas.dto.RecargaRequest;
import co.com.recargas.dto.RecargaResponse;
import co.com.recargas.dto.ResumenRecargasResponse;
import co.com.recargas.service.RecargaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recargas")
public class RecargaController {

    private final RecargaService recargaService;

    public RecargaController(RecargaService recargaService) {
        this.recargaService = recargaService;
    }

    /** Registra la venta de una recarga. */
    @PostMapping
    public ResponseEntity<RecargaResponse> vender(@Valid @RequestBody RecargaRequest solicitud) {
        RecargaResponse recarga = recargaService.vender(solicitud);
        return ResponseEntity.created(URI.create("/api/recargas/" + recarga.id())).body(recarga);
    }

    /** Últimas recargas vendidas (más recientes primero). */
    @GetMapping
    public List<RecargaResponse> ultimas(@RequestParam(defaultValue = "20") int limite) {
        return recargaService.ultimas(limite);
    }

    /** Cantidad y valor de recargas por operador y por vendedor, opcionalmente en un rango de fechas. */
    @GetMapping("/resumen")
    public ResumenRecargasResponse resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return recargaService.resumen(desde, hasta);
    }
}
