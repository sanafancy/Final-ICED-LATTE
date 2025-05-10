package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
public class Cliente extends Usuario{
    @Column
    private String nombre;
    @Column
    private String apellidos;
    @Column
    private String dni;
    @ManyToMany
    @JoinTable(
            name = "cliente_favoritos",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurante_id")
    )
    private Set<Restaurante> favoritos = new HashSet<>();
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Pedido> pedidos;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "direccion_cliente",  // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "direccion_id")
    )
    private List<Direccion> direcciones = new ArrayList<>();


    public Cliente() {}

    public Cliente(String email, String pass,String nombre, String apellidos, String dni) {
        super (email, pass);
        this.nombre = nombre;
        this.apellidos=apellidos;
        this.dni=dni;
        //this.direcciones=direcciones;
    }

    // Getters y Setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Set<Restaurante> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(Set<Restaurante> favoritos) {
        this.favoritos = favoritos;
    }


    public List<Pedido> getPedidos() {
        return pedidos;
    }
    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    public List<Direccion> getDirecciones() {
        return direcciones;
    }
    public void setDirecciones(List<Direccion> direcciones) {
        this.direcciones = direcciones;
    }

    @Override
    public String toString() {
        return String.format("Cliente [idUsuario=%s, pass=%s, nombre=%s, apellidos=%s, dni=%s]", getIdUsuario(), getPass(), nombre, apellidos, dni);
    }
}