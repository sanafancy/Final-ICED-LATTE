package proyecto.iso2.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import proyecto.iso2.dominio.entidades.*    ;

import java.util.List;
@Repository
public interface PedidoDAO extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCliente(Cliente cliente);
    List<Pedido> findByRestaurante(Restaurante restaurante);
}
