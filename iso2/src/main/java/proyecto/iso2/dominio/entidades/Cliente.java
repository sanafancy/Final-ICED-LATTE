package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;


@Entity
public class Cliente extends Usuario{
    @Column
    private String nombre;
    @Column
    private String apellidos;
    @Column
    private String dni;
    @ManyToMany(fetch = FetchType.EAGER) //Cargar la relación inmediatamente
    @JoinTable(
            name = "cliente_favoritos",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurante_id")
    )
    private List<Restaurante> favoritos = new ArrayList<>(); //creamos una nueva tabla lista usuario - favorito

    public Cliente() {}

    public Cliente(String email, String pass,String nombre, String apellidos, String dni) {
        super (email, pass);
        this.nombre = nombre;
        this.apellidos=apellidos;
        this.dni=dni;
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

    public List<Restaurante> getFavoritos() {
        if (favoritos == null) {
            favoritos = new ArrayList<>();
        }
        return favoritos;
    }

    public void setFavoritos(List<Restaurante> favoritos) {
        this.favoritos = favoritos != null ? favoritos : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("Cliente [idUsuario=%s, pass=%s, nombre=%s, apellidos=%s, dni=%s]", getIdUsuario(), getPass(), nombre, apellidos, dni);
    }
}