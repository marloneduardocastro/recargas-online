package co.com.recargas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recarga vendida: quién la vendió, a qué operador, a qué número y por cuánto.
 */
@Entity
@Table(name = "recarga", indexes = {
        @Index(name = "ix_recarga_operador", columnList = "operador_id"),
        @Index(name = "ix_recarga_vendedor", columnList = "vendedor_id"),
        @Index(name = "ix_recarga_fecha", columnList = "fecha_venta")
})
public class Recarga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private Operador operador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;

    @Column(name = "numero_celular", nullable = false, length = 10)
    private String numeroCelular;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    protected Recarga() {
        // requerido por JPA
    }

    public Recarga(Operador operador, Vendedor vendedor, String numeroCelular, BigDecimal valor) {
        this.operador = operador;
        this.vendedor = vendedor;
        this.numeroCelular = numeroCelular;
        this.valor = valor;
    }

    @PrePersist
    void antesDeGuardar() {
        if (fechaVenta == null) {
            fechaVenta = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Operador getOperador() {
        return operador;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }
}
