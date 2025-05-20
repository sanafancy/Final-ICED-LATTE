package proyecto.iso2.persistencia;

import proyecto.iso2.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailAndPass(String email, String pass);
    List<Usuario> findAll();
}