package proyecto.iso2.persistencia;

import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionDAO extends JpaRepository<Direccion, Long> {
    List<Direccion> findByRestaurante(Restaurante restaurante);
}