package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
public class ServicioEntrega {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;
    @ManyToOne
    @JoinColumn(name = "direccion_id", nullable = false)
    private Direccion direccion;
    @ManyToOne
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Repartidor repartidor;
    @Column
    private LocalDateTime fechaRecepcion;
    @Column
    private LocalDateTime fechaEntrega;


    public ServicioEntrega(Long id, Pedido pedido, Direccion direccion, Repartidor repartidor, LocalDateTime fechaRecepcion, LocalDateTime fechaEntrega) {
        this.id = id;
        this.pedido = pedido;
        this.direccion = direccion;
        this.repartidor = repartidor;
        this.fechaRecepcion = fechaRecepcion;
        this.fechaEntrega = fechaEntrega;
    }
    public ServicioEntrega() {   }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Pedido getPedido() { return pedido;}
    public void setPedido(Pedido pedido) { this.pedido = pedido;}

    public Direccion getDireccion() { return direccion;}
    public void setDireccion(Direccion direccion) {this.direccion = direccion;}

    public Repartidor getRepartidor() {return repartidor;}
    public void setRepartidor(Repartidor repartidor) {this.repartidor = repartidor;}

    public LocalDateTime getFechaRecepcion() {return fechaRecepcion;}
    public void setFechaRecepcion(LocalDateTime fechaRecepcion) {this.fechaRecepcion = fechaRecepcion;}

    public LocalDateTime getFechaEntrega() {return fechaEntrega;}
    public void setFechaEntrega(LocalDateTime fechaEntrega) {this.fechaEntrega = fechaEntrega;}
}
