package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proyecto.iso2.dominio.entidades.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RestauranteDAO extends JpaRepository <Restaurante, Long> {
    List<Restaurante> findByNombreContainingIgnoreCase(String nombre);
    List<Restaurante> findByDireccion_CalleContainingIgnoreCase(String calle);
    List<Restaurante> findByDireccion_MunicipioContainingIgnoreCase(String municipio);
    List<Restaurante> findByDireccion_CodigoPostal(int codigoPostal);
}