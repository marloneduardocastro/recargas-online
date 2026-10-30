package co.com.recargas.service;

import co.com.recargas.domain.Operador;
import co.com.recargas.domain.Recarga;
import co.com.recargas.domain.Vendedor;
import co.com.recargas.dto.RecargaRequest;
import co.com.recargas.dto.RecargaResponse;
import co.com.recargas.dto.ResumenRecargasResponse;
import co.com.recargas.dto.TotalOperadorVendedor;
import co.com.recargas.dto.TotalRecargas;
import co.com.recargas.exception.RecursoNoEncontradoException;
import co.com.recargas.exception.ReglaNegocioException;
import co.com.recargas.repository.OperadorRepository;
import co.com.recargas.repository.RecargaRepository;
import co.com.recargas.repository.VendedorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reglas de negocio de la venta de recargas y de su consulta consolidada.
 */
@Service
public class RecargaService {

    private static final int MAXIMO_ULTIMAS = 100;
    private static final LocalDateTime FECHA_MINIMA = LocalDateTime.of(1900, 1, 1, 0, 0);

    private final RecargaRepository recargaRepository;
    private final OperadorRepository operadorRepository;
    private final VendedorRepository vendedorRepository;

    public RecargaService(RecargaRepository recargaRepository,
                          OperadorRepository operadorRepository,
                          VendedorRepository vendedorRepository) {
        this.recargaRepository = recargaRepository;
        this.operadorRepository = operadorRepository;
        this.vendedorRepository = vendedorRepository;
    }

    @Transactional
    public RecargaResponse vender(RecargaRequest solicitud) {
        Operador operador = operadorRepository.findById(solicitud.operadorId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el operador con id " + solicitud.operadorId()));
        if (!operador.isActivo()) {
            throw new ReglaNegocioException("El operador " + operador.getNombre() + " no está habilitado para la venta");
        }

        Vendedor vendedor = vendedorRepository.findById(solicitud.vendedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el vendedor con id " + solicitud.vendedorId()));
        if (!vendedor.isActivo()) {
            throw new ReglaNegocioException("El vendedor " + vendedor.getNombre() + " no está habilitado para vender");
        }

        Recarga recarga = recargaRepository.save(
                new Recarga(operador, vendedor, solicitud.numeroCelular(), solicitud.valor()));
        return RecargaResponse.from(recarga);
    }

    @Transactional(readOnly = true)
    public List<RecargaResponse> ultimas(int limite) {
        int tamano = Math.min(Math.max(limite, 1), MAXIMO_ULTIMAS);
        return recargaRepository.buscarUltimas(PageRequest.of(0, tamano)).stream()
                .map(RecargaResponse::from)
                .toList();
    }

    /**
     * Cantidad y valor de recargas por operador, por vendedor y por ambos.
     * Si no se envían fechas se totaliza todo lo vendido hasta hoy.
     */
    @Transactional(readOnly = true)
    public ResumenRecargasResponse resumen(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser mayor que la fecha final");
        }
        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : FECHA_MINIMA;
        LocalDateTime fin = (hasta != null ? hasta : LocalDate.now()).plusDays(1).atStartOfDay();

        // Se incluyen en cero los operadores y vendedores sin ventas en el rango
        Map<Long, TotalRecargas> ventasOperador = porId(recargaRepository.totalizarPorOperador(inicio, fin));
        List<TotalRecargas> porOperador = operadorRepository.findAll(Sort.by("nombre")).stream()
                .map(o -> ventasOperador.getOrDefault(o.getId(), TotalRecargas.enCero(o.getId(), o.getNombre())))
                .toList();

        Map<Long, TotalRecargas> ventasVendedor = porId(recargaRepository.totalizarPorVendedor(inicio, fin));
        List<TotalRecargas> porVendedor = vendedorRepository.findAll(Sort.by("nombre")).stream()
                .map(v -> ventasVendedor.getOrDefault(v.getId(), TotalRecargas.enCero(v.getId(), v.getNombre())))
                .toList();

        List<TotalOperadorVendedor> porOperadorYVendedor =
                recargaRepository.totalizarPorOperadorYVendedor(inicio, fin);

        long cantidadTotal = porOperador.stream().mapToLong(TotalRecargas::cantidad).sum();
        BigDecimal valorTotal = porOperador.stream()
                .map(TotalRecargas::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenRecargasResponse(desde, hasta, cantidadTotal, valorTotal,
                porOperador, porVendedor, porOperadorYVendedor);
    }

    private static Map<Long, TotalRecargas> porId(List<TotalRecargas> totales) {
        return totales.stream().collect(Collectors.toMap(TotalRecargas::id, Function.identity()));
    }
}
