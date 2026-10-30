package co.com.recargas.dto;

import java.math.BigDecimal;

/**
 * Cantidad y valor de recargas por la combinación operador + vendedor.
 */
public record TotalOperadorVendedor(String operador, String vendedor, Long cantidad, BigDecimal valorTotal) {
}
