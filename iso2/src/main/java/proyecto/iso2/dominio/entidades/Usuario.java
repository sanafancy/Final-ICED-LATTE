package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    @Column(unique = true)
    private String email;
    @Column
    private String pass;

    public Usuario(){}
    public Usuario(String email, String pass){
        this.email=email;
        this.pass=pass;
    }
    // Getters y Setters
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    @Override
    public String toString() {
        return String.format("Usuario [idUsuario=%s, email=%s, pass=%s]", idUsuario, email, pass);
    }
}