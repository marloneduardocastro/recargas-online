package co.com.recargas.dto;

import java.math.BigDecimal;

/**
 * Cantidad y valor de recargas agrupadas por un solo criterio (operador o vendedor).
 */
public record TotalRecargas(Long id, String nombre, Long cantidad, BigDecimal valorTotal) {

    public static TotalRecargas enCero(Long id, String nombre) {
        return new TotalRecargas(id, nombre, 0L, BigDecimal.ZERO);
    }
}
