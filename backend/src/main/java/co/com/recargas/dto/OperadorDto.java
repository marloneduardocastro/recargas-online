package co.com.recargas.dto;

import co.com.recargas.domain.Operador;

public record OperadorDto(Long id, String nombre) {

    public static OperadorDto from(Operador operador) {
        return new OperadorDto(operador.getId(), operador.getNombre());
    }
}
