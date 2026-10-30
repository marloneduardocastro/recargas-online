package co.com.recargas.repository;

import co.com.recargas.domain.Vendedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendedorRepository extends JpaRepository<Vendedor, Long> {

    List<Vendedor> findByActivoTrueOrderByNombreAsc();
}
