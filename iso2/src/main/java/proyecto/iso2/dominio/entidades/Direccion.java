package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;
@Entity
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String calle;
    @Column
    private int numero;
    @Column
    private String complemento;
    @Column
    private int codigoPostal;
    @Column
    private String municipio;
    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    public Direccion() {    }
    public Direccion(String calle, int numero, String complemento, int codigoPostal, String municipio, Restaurante restaurante) {
        this.calle = calle;
        this.numero = numero;
        this.complemento = complemento;
        this.codigoPostal = codigoPostal;
        this.municipio = municipio;
        this.restaurante = restaurante;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }
    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public int getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(int codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

}
