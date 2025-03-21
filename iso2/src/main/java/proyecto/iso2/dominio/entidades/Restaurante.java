package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;


@Entity
public class Restaurante extends Usuario{
    @Column
    private String nombre;
    @Column
    private String cif;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Direccion direccion;

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

    @Override
    public String toString() {
        return String.format("Restaurante [idUsuario=%s, pass=%s, nombre=%s, cif=%s]", getIdUsuario(), getPass(), nombre, cif);
    }

}