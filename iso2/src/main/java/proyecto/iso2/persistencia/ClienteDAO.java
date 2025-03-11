package proyecto.iso2.persistencia;
import proyecto.iso2.dominio.entidades.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClienteDAO extends JpaRepository<Cliente, Long> {
}