package proyecto.iso2.persistencia;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Pedido;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.dominio.entidades.EstadoPedido; // Importar el enum
import proyecto.iso2.dominio.entidades.MetodoPago; // Importar el enum

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PedidoDAOIntegrationTest {

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Limpiamos la base de datos respetando las dependencias
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM Pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });
    }

    @Test
    public void testCrudPedido() {
        // Crear un cliente
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        clienteDAO.save(cliente);
        entityManager.flush();

        // Crear una dirección para el restaurante
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        entityManager.persist(direccion);
        entityManager.flush();

        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante.setDireccion(direccion); // Asignamos la dirección
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear un pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PEDIDO);
        pedido.setMetodoPago(MetodoPago.CREDIT_CARD);

        // Guardar el pedido
        pedidoDAO.save(pedido);
        entityManager.flush();

        // Verificar que se ha guardado
        List<Pedido> pedidos = pedidoDAO.findAll();
        assertThat(pedidos, hasSize(1));
        assertThat(pedidos.get(0).getCliente().getNombre(), is("Cliente A"));
        assertThat(pedidos.get(0).getRestaurante().getNombre(), is("Restaurante A"));
        assertThat(pedidos.get(0).getEstado(), is(EstadoPedido.PEDIDO)); // Ajustamos la aserción
        assertThat(pedidos.get(0).getMetodoPago(), is(MetodoPago.CREDIT_CARD));

        // Actualizar el pedido
        Pedido pedidoGuardado = pedidos.get(0);
        pedidoGuardado.setEstado(EstadoPedido.PAGADO);
        pedidoDAO.save(pedidoGuardado);
        entityManager.flush();

        // Verificar la actualización
        Pedido pedidoActualizado = pedidoDAO.findById(pedidoGuardado.getId()).orElse(null);
        assertThat(pedidoActualizado, notNullValue());
        assertThat(pedidoActualizado.getEstado(), is(EstadoPedido.PAGADO));

        // Eliminar el pedido
        pedidoDAO.delete(pedidoActualizado);
        entityManager.flush();

        // Verificar la eliminación
        assertThat(pedidoDAO.findAll(), hasSize(0));
    }

    @Test
    public void testFindByCliente() {
        // Crear un cliente
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        clienteDAO.save(cliente);
        entityManager.flush();

        // Crear una dirección para el restaurante
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        entityManager.persist(direccion);
        entityManager.flush();

        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante.setDireccion(direccion); // Asignamos la dirección
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear dos pedidos para el cliente
        Pedido pedido1 = new Pedido();
        pedido1.setCliente(cliente);
        pedido1.setRestaurante(restaurante);
        pedido1.setFecha(LocalDateTime.now());
        pedido1.setEstado(EstadoPedido.PEDIDO);
        pedido1.setMetodoPago(MetodoPago.CREDIT_CARD);
        pedidoDAO.save(pedido1);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente(cliente);
        pedido2.setRestaurante(restaurante);
        pedido2.setFecha(LocalDateTime.now());
        pedido2.setEstado(EstadoPedido.ENTREGADO);
        pedido2.setMetodoPago(MetodoPago.PAYPAL);
        pedidoDAO.save(pedido2);

        entityManager.flush();

        // Buscar pedidos por cliente
        List<Pedido> pedidos = pedidoDAO.findByCliente(cliente);
        assertThat(pedidos, hasSize(2));
        assertThat(pedidos, containsInAnyOrder(
                hasProperty("estado", is(EstadoPedido.PEDIDO)),
                hasProperty("estado", is(EstadoPedido.ENTREGADO))
        ));
    }

    @Test
    public void testFindByRestaurante() {
        // Crear un cliente
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        clienteDAO.save(cliente);
        entityManager.flush();

        // Crear una dirección para el restaurante
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        entityManager.persist(direccion);
        entityManager.flush();

        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante.setDireccion(direccion); // Asignamos la dirección
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear dos pedidos para el restaurante
        Pedido pedido1 = new Pedido();
        pedido1.setCliente(cliente);
        pedido1.setRestaurante(restaurante);
        pedido1.setFecha(LocalDateTime.now());
        pedido1.setEstado(EstadoPedido.PEDIDO);
        pedido1.setMetodoPago(MetodoPago.CREDIT_CARD);
        pedidoDAO.save(pedido1);

        Pedido pedido2 = new Pedido();
        pedido2.setCliente(cliente);
        pedido2.setRestaurante(restaurante);
        pedido2.setFecha(LocalDateTime.now());
        pedido2.setEstado(EstadoPedido.ENTREGADO);
        pedido2.setMetodoPago(MetodoPago.PAYPAL);
        pedidoDAO.save(pedido2);

        entityManager.flush();

        // Buscar pedidos por restaurante
        List<Pedido> pedidos = pedidoDAO.findByRestaurante(restaurante);
        assertThat(pedidos, hasSize(2));
        assertThat(pedidos, containsInAnyOrder(
                hasProperty("estado", is(EstadoPedido.PEDIDO)),
                hasProperty("estado", is(EstadoPedido.ENTREGADO))
        ));
    }
}