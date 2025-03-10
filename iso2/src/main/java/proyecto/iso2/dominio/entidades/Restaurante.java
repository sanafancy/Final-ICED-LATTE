package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Restaurante extends Usuario{
    @Column
    private String nombre;
    @Column
    private String cif;

    public Restaurante() {}
    // Constructor
    public Restaurante(String email,String pass,String  nombre, String cif) {
        super(email, pass);
        this.nombre = nombre;
        this.cif = cif;
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

    @Override
    public String toString() {
        return String.format("Restaurante [idUsuario=%s, pass=%s, nombre=%s, cif=%s]", getIdUsuario(), getPass(), nombre, cif);
    }

}