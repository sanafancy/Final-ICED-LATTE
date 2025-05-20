package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.Restaurante;
import java.util.List;

@Repository
public interface CartaMenuDAO extends JpaRepository<CartaMenu, Long> {
    List<CartaMenu> findByRestaurante(Restaurante restaurante);
    @Transactional
    void deleteByRestaurante(Restaurante restaurante);
}