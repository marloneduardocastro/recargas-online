package co.com.recargas.dto;

import co.com.recargas.domain.Recarga;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecargaResponse(
        Long id,
        Long operadorId,
        String operador,
        Long vendedorId,
        String vendedor,
        String numeroCelular,
        BigDecimal valor,
        LocalDateTime fechaVenta
) {

    public static RecargaResponse from(Recarga recarga) {
        return new RecargaResponse(
                recarga.getId(),
                recarga.getOperador().getId(),
                recarga.getOperador().getNombre(),
                recarga.getVendedor().getId(),
                recarga.getVendedor().getNombre(),
                recarga.getNumeroCelular(),
                recarga.getValor(),
                recarga.getFechaVenta());
    }
}
