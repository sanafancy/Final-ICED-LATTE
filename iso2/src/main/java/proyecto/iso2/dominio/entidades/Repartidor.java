package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;

@Entity
public class Repartidor extends Usuario {
    @Column
    private String nombre;
    @Column
    private String apellidos;
    @Column
    private String nif;
    @Column
    private int eficiencia;

    public Repartidor() {}

    public Repartidor(String email, String  pass, String nombre, String apellidos, String nif, int eficiencia) {
        super(email, pass);
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.nif = nif;
        this.eficiencia = eficiencia;
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

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public int getEficiencia() {
        return eficiencia;
    }

    public void setEficiencia(int eficiencia) {
        this.eficiencia = eficiencia;
    }

    @Override
    public String toString() {
        return String.format("Repartidor [idUsuario=%s, pass=%s, nombre=%s, apellidos=%s, nif=%s, eficiencia=%s]", getIdUsuario(), getPass(), nombre, apellidos, nif, eficiencia);
    }
}