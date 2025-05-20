package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.ServicioEntrega;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioEntregaDAO extends JpaRepository<ServicioEntrega, Long> {
    List<ServicioEntrega> findByDireccionAndFechaEntregaIsNull(Direccion direccion);
    List<ServicioEntrega> findByRepartidor_IdUsuario(Long idUsuario);
}
