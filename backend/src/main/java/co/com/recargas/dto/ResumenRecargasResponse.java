package co.com.recargas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Totales de recargas para el rango consultado, discriminados por operador,
 * por vendedor y por la combinación de ambos.
 */
public record ResumenRecargasResponse(
        LocalDate desde,
        LocalDate hasta,
        long cantidadTotal,
        BigDecimal valorTotal,
        List<TotalRecargas> porOperador,
        List<TotalRecargas> porVendedor,
        List<TotalOperadorVendedor> porOperadorYVendedor
) {
}
