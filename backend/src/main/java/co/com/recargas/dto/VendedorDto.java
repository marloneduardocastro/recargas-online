package co.com.recargas.dto;

import co.com.recargas.domain.Vendedor;

public record VendedorDto(Long id, String numeroDocumento, String nombre) {

    public static VendedorDto from(Vendedor vendedor) {
        return new VendedorDto(vendedor.getId(), vendedor.getNumeroDocumento(), vendedor.getNombre());
    }
}
