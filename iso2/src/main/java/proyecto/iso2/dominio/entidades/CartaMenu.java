package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class CartaMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @ManyToOne
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;
    @OneToMany(mappedBy = "cartaMenu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemMenu> items;

    public CartaMenu(){}

    public CartaMenu(String nombre, Restaurante restaurante) {
        this.nombre = nombre;
        this.restaurante = restaurante;
    }
    // los getters y setters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }
    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public List<ItemMenu> getItems() {
        return items;
    }
    public void setItems(List<ItemMenu> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return String.format("CartaMenu [id=%s, nombre=%s]", id, nombre);
    }
}