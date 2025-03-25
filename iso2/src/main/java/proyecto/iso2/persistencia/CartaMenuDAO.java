package proyecto.iso2.persistencia;

import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CartaMenuDAO extends JpaRepository<CartaMenu, Long> {
    List<CartaMenu> findByRestaurante(Restaurante restaurante);
}