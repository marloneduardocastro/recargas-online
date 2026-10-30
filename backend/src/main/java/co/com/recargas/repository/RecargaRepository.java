package co.com.recargas.repository;

import co.com.recargas.domain.Recarga;
import co.com.recargas.dto.TotalOperadorVendedor;
import co.com.recargas.dto.TotalRecargas;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecargaRepository extends JpaRepository<Recarga, Long> {

    @Query("""
            SELECT r FROM Recarga r
              JOIN FETCH r.operador
              JOIN FETCH r.vendedor
             ORDER BY r.fechaVenta DESC, r.id DESC
            """)
    List<Recarga> buscarUltimas(Pageable pageable);

    @Query("""
            SELECT new co.com.recargas.dto.TotalRecargas(o.id, o.nombre, COUNT(r), SUM(r.valor))
              FROM Recarga r JOIN r.operador o
             WHERE r.fechaVenta >= :desde AND r.fechaVenta < :hasta
             GROUP BY o.id, o.nombre
            """)
    List<TotalRecargas> totalizarPorOperador(@Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT new co.com.recargas.dto.TotalRecargas(v.id, v.nombre, COUNT(r), SUM(r.valor))
              FROM Recarga r JOIN r.vendedor v
             WHERE r.fechaVenta >= :desde AND r.fechaVenta < :hasta
             GROUP BY v.id, v.nombre
            """)
    List<TotalRecargas> totalizarPorVendedor(@Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta);

    @Query("""
            SELECT new co.com.recargas.dto.TotalOperadorVendedor(o.nombre, v.nombre, COUNT(r), SUM(r.valor))
              FROM Recarga r JOIN r.operador o JOIN r.vendedor v
             WHERE r.fechaVenta >= :desde AND r.fechaVenta < :hasta
             GROUP BY o.nombre, v.nombre
             ORDER BY o.nombre, v.nombre
            """)
    List<TotalOperadorVendedor> totalizarPorOperadorYVendedor(@Param("desde") LocalDateTime desde,
                                                              @Param("hasta") LocalDateTime hasta);
}
