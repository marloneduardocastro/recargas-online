package co.com.recargas.repository;

import co.com.recargas.domain.Operador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperadorRepository extends JpaRepository<Operador, Long> {

    List<Operador> findByActivoTrueOrderByNombreAsc();
}
