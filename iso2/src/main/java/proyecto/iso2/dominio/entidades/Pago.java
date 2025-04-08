package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;

import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Pago {

    @Id
    @Column(name = "id_transaccion", columnDefinition = "BINARY(16)")
    private UUID idTransaccion;

    @Column(name = "fecha_transaccion")
    private LocalDate fechaTransaccion;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @OneToOne
    @JoinColumn(name = "pedido_id", referencedColumnName = "id")
    private Pedido pedido;

    public Pago() {
        this.idTransaccion = UUID.randomUUID();
        this.fechaTransaccion = LocalDate.now();
    }

    public Pago(MetodoPago metodoPago, Pedido pedido) {
        this.idTransaccion = UUID.randomUUID();
        this.fechaTransaccion = LocalDate.now();
        this.metodoPago = metodoPago;
        this.pedido = pedido;
    }

    public UUID getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(UUID idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public LocalDate getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(LocalDate fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
