package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Restaurante extends Usuario{
    @Column
    private String nombre;
    @Column
    private String cif;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "direccion_id", referencedColumnName = "id")
    private Direccion direccion;
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;
    @ManyToMany(mappedBy = "favoritos")
    private Set<Cliente> clientesFavoritos;

    public Restaurante() {}
    // Constructor
    public Restaurante(String email,String pass,String  nombre, String cif, Direccion direccion) {
        super(email, pass);
        this.nombre = nombre;
        this.cif = cif;
        this.direccion = direccion;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCif() {
        return cif;
    }
    public void setCif(String cif) {
        this.cif = cif;
    }

    public Direccion getDireccion() {return direccion;}
    public void setDireccion(Direccion direccion) {this.direccion = direccion;}

    public List<Pedido> getPedidos() {
        return pedidos;
    }
    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public String toString() {
        return String.format("Restaurante [idUsuario=%s, pass=%s, nombre=%s, cif=%s]", getIdUsuario(), getPass(), nombre, cif);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurante)) return false;
        Restaurante that = (Restaurante) o;
        return getIdUsuario().equals(that.getIdUsuario());
    }

    @Override
    public int hashCode() {
        return getIdUsuario().hashCode();
    }

}