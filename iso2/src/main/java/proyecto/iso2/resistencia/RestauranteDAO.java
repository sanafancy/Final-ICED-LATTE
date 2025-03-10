package proyecto.iso2.resistencia;

import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RestauranteDAO extends JpaRepository <Restaurante, Long> {

}