package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    @Transactional
    @Modifying
    @Query("DELETE FROM ItemMenu i WHERE i.id = :itemId")
    void deleteById(@Param("itemId") Long itemId);
}