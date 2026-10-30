package co.com.recargas.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Datos que envía la pantalla para vender una recarga.
 */
public record RecargaRequest(

        @NotNull(message = "El operador es obligatorio")
        Long operadorId,

        @NotNull(message = "El vendedor es obligatorio")
        Long vendedorId,

        @NotBlank(message = "El número celular es obligatorio")
        @Pattern(regexp = "^3\\d{9}$", message = "El número celular debe tener 10 dígitos y empezar por 3")
        String numeroCelular,

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "1000", message = "El valor mínimo de una recarga es $1.000")
        @DecimalMax(value = "500000", message = "El valor máximo de una recarga es $500.000")
        BigDecimal valor
) {
}
