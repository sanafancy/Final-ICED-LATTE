package proyecto.iso2.resistencia;

import proyecto.iso2.dominio.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UsuarioDAO extends JpaRepository<Usuario, Long> {
}