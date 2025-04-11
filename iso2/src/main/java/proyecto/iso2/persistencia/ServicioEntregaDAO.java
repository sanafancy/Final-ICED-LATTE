package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.iso2.dominio.entidades.ServicioEntrega;

@Repository
public interface ServicioEntregaDAO extends JpaRepository<ServicioEntrega, Long> {
}
