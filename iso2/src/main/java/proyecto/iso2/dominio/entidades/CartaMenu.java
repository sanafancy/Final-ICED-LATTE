package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class CartaMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String nombre;
    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<ItemMenu> items;

    public CartaMenu(){}

    public CartaMenu(String nombre, Restaurante restaurante) {
        this.nombre = nombre;
        this.restaurante = restaurante;
    }
    // los getters y setters
    public String getNombre() {
        return nombre;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public Long getId() {
        return id;
    }

    public List<ItemMenu> getItems() {
        return items;
    }

    public void setItems(List<ItemMenu> items) {
        this.items = items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    @Override
    public String toString() {
        return String.format("CartaMenu [id=%s, nombre=%s]", id, nombre);
    }
}