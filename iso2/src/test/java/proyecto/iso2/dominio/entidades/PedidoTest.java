
package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

    public class PedidoTest {

        private Pedido pedido;

        @BeforeEach
        public void setUp() {
            Restaurante restaurante = new Restaurante("rest@example.com", "pass123", "Restaurante Uno", "CIF123", null);
            Cliente cliente = new Cliente("cli@example.com", "pass456", "Juan", "12345678A", null);
            //List<ItemMenu> items = new ArrayList<>();
            pedido = new Pedido(cliente, restaurante);
        }

        @Test
        public void testEstadoInicial() {
            //assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        }

        @Test
        public void testCambiarEstado() {
            pedido.setEstado(EstadoPedido.ENTREGADO);
            assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
        }

        @Test
        public void testAsociacionCliente() {
            assertNotNull(pedido.getCliente());
            assertEquals("cli@example.com", pedido.getCliente().getEmail());
        }

        @Test
        public void testAsociacionRestaurante() {
            assertNotNull(pedido.getRestaurante());
            assertEquals("Restaurante Uno", pedido.getRestaurante().getNombre());
        }
    }


