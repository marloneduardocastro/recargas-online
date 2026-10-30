package co.com.recargas.web;

import co.com.recargas.dto.ResumenRecargasResponse;
import co.com.recargas.dto.TotalRecargas;
import co.com.recargas.repository.OperadorRepository;
import co.com.recargas.repository.VendedorRepository;
import co.com.recargas.service.RecargaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecargaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecargaService recargaService;

    @Autowired
    private OperadorRepository operadorRepository;

    @Autowired
    private VendedorRepository vendedorRepository;

    @Test
    void listaLosCuatroOperadores() throws Exception {
        mockMvc.perform(get("/api/operadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void vendeUnaRecargaYLaRetornaConElOperadorYVendedor() throws Exception {
        long tigo = idOperador("Tigo");
        long ana = idVendedor("1010101010");

        mockMvc.perform(post("/api/recargas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tigo, ana, "3001112233", 15000)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.operador").value("Tigo"))
                .andExpect(jsonPath("$.vendedor").value("Ana Gómez"))
                .andExpect(jsonPath("$.valor").value(15000))
                .andExpect(jsonPath("$.fechaVenta").exists());
    }

    @Test
    void rechazaNumeroCelularYValorInvalidos() throws Exception {
        mockMvc.perform(post("/api/recargas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(idOperador("Tigo"), idVendedor("1010101010"), "12345", 500)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroCelular").exists())
                .andExpect(jsonPath("$.errores.valor").exists());
    }

    @Test
    void retorna404SiElOperadorNoExiste() throws Exception {
        mockMvc.perform(post("/api/recargas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(9999, idVendedor("1010101010"), "3001112233", 5000)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el operador con id 9999"));
    }

    @Test
    void elResumenSumaLaVentaEnElOperadorYEnElVendedor() throws Exception {
        ResumenRecargasResponse antes = recargaService.resumen(null, null);

        mockMvc.perform(post("/api/recargas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(idOperador("Uff"), idVendedor("2020202020"), "3204445566", 7000)))
                .andExpect(status().isCreated());

        ResumenRecargasResponse despues = recargaService.resumen(null, null);

        assertThat(despues.cantidadTotal()).isEqualTo(antes.cantidadTotal() + 1);
        assertThat(despues.valorTotal()).isEqualByComparingTo(antes.valorTotal().add(new BigDecimal("7000")));

        TotalRecargas uffAntes = buscar(antes, "Uff", true);
        TotalRecargas uffDespues = buscar(despues, "Uff", true);
        assertThat(uffDespues.cantidad()).isEqualTo(uffAntes.cantidad() + 1);
        assertThat(uffDespues.valorTotal()).isEqualByComparingTo(uffAntes.valorTotal().add(new BigDecimal("7000")));

        TotalRecargas carlosDespues = buscar(despues, "Carlos Pérez", false);
        TotalRecargas carlosAntes = buscar(antes, "Carlos Pérez", false);
        assertThat(carlosDespues.cantidad()).isEqualTo(carlosAntes.cantidad() + 1);

        assertThat(despues.porOperadorYVendedor())
                .anySatisfy(t -> {
                    assertThat(t.operador()).isEqualTo("Uff");
                    assertThat(t.vendedor()).isEqualTo("Carlos Pérez");
                });
    }

    @Test
    void elResumenIncluyeOperadoresSinVentasEnCero() throws Exception {
        String manana = LocalDate.now().plusDays(1).toString();

        mockMvc.perform(get("/api/recargas/resumen").param("desde", manana).param("hasta", manana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadTotal").value(0))
                .andExpect(jsonPath("$.porOperador", hasSize(4)))
                .andExpect(jsonPath("$.porOperador[0].cantidad").value(0))
                .andExpect(jsonPath("$.porOperadorYVendedor", hasSize(0)));
    }

    @Test
    void rechazaRangoDeFechasInvertido() throws Exception {
        mockMvc.perform(get("/api/recargas/resumen").param("desde", "2026-12-31").param("hasta", "2026-01-01"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listaLasUltimasRecargas() throws Exception {
        mockMvc.perform(get("/api/recargas").param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operador").exists());

        mockMvc.perform(get("/api/recargas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(4)));
    }

    private long idOperador(String nombre) {
        return operadorRepository.findAll().stream()
                .filter(o -> o.getNombre().equals(nombre))
                .findFirst().orElseThrow().getId();
    }

    private long idVendedor(String documento) {
        return vendedorRepository.findAll().stream()
                .filter(v -> v.getNumeroDocumento().equals(documento))
                .findFirst().orElseThrow().getId();
    }

    private static TotalRecargas buscar(ResumenRecargasResponse resumen, String nombre, boolean operador) {
        return (operador ? resumen.porOperador() : resumen.porVendedor()).stream()
                .filter(t -> t.nombre().equals(nombre))
                .findFirst().orElseThrow();
    }

    private static String json(long operadorId, long vendedorId, String celular, long valor) {
        return """
                {"operadorId": %d, "vendedorId": %d, "numeroCelular": "%s", "valor": %d}
                """.formatted(operadorId, vendedorId, celular, valor);
    }
}
