package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.Restaurante;
import java.util.List;

public interface CartaMenuDAO extends JpaRepository<CartaMenu, Long> {
    /** Busca todas las cartas que pertenecen al restaurante dado */
    List<CartaMenu> findByCartaPadre(CartaMenu cartaPadre);
    List<CartaMenu> findByRestaurante(Restaurante restaurante);
    List<CartaMenu> findByRestauranteAndCartaPadreIsNull(Restaurante restaurante);

}