package proyecto.iso2.persistencia;

import proyecto.iso2.dominio.entidades.CodigoPostal;
import proyecto.iso2.dominio.entidades.Repartidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepartidorDAO extends JpaRepository<Repartidor, Long> {
    List<Repartidor> findByCodigoPostal(CodigoPostal codigoPostal);

    List<Repartidor> findByCodigoPostalOrderByEficienciaAsc(CodigoPostal codigoPostal);
}