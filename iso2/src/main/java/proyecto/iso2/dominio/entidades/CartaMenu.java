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

    public CartaMenu(){}

    public CartaMenu(String nombre){
        this.nombre = nombre;
    }
    // los getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return String.format("CartaMenu [id=%s, nombre=%s]", id, nombre);
    }
}