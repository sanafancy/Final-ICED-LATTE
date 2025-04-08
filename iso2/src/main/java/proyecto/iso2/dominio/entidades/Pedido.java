package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private ServicioEntrega servicioEntrega;
    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;
    @Column
    private LocalDateTime fecha;
    @ManyToMany
    @JoinTable(
            name = "pedido_items",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id")
    )

    private List<ItemMenu> items;

    public Pedido() {}
    public Pedido(Cliente cliente, Restaurante restaurante, ServicioEntrega servicioEntrega) {
        this.cliente = cliente;
        this.restaurante = restaurante;
        this.servicioEntrega = servicioEntrega;
    }

    public Pedido(Cliente cliente, Direccion direccion, Double total) {
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }
    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public EstadoPedido getEstado() {
        return estado;
    }
    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }
    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public List<ItemMenu> getItems() {
        return items;
    }
    public void setItems(List<ItemMenu> items) {
        this.items = items;
    }

    public ServicioEntrega getServicioEntrega() {return servicioEntrega;}
    public void setServicioEntrega(ServicioEntrega servicioEntrega) {this.servicioEntrega = servicioEntrega;}
}
