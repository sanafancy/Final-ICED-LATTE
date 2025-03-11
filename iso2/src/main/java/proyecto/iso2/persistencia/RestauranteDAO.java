package proyecto.iso2.persistencia;

import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestauranteDAO extends JpaRepository <Restaurante, Long> {

}