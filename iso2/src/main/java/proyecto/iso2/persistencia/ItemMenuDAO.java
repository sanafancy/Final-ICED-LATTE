package proyecto.iso2.persistencia;

import org.springframework.transaction.annotation.Transactional;
import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMenuDAO extends JpaRepository<ItemMenu, Long> {
    List<ItemMenu> findByCartaMenu(CartaMenu cartaMenu);
    @Transactional
    void deleteByCartaMenu(CartaMenu carta);
}