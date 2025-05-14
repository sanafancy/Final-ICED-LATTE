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
    private Integer eficiencia;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "codigo_postal")
    private CodigoPostal codigoPostal;

    public Repartidor() {}

    public Repartidor(String email, String pass, String nombre, String apellidos, String nif, Integer eficiencia) {
        super(email, pass);
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.nif = nif;
        this.eficiencia = eficiencia;
    }

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

    public Integer getEficiencia() {
        return eficiencia;
    }

    public void setEficiencia(Integer eficiencia) {
        this.eficiencia = eficiencia;
    }

    public CodigoPostal getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(CodigoPostal codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public void incrementarEficiencia() {
        this.eficiencia += 1;
    }

    @Override
    public String toString() {
        return String.format("Repartidor [idUsuario=%s, pass=%s, nombre=%s, apellidos=%s, nif=%s, eficiencia=%s, codigoPostal=%s]", getIdUsuario(), getPass(), nombre, apellidos, nif, eficiencia, codigoPostal);
    }
}