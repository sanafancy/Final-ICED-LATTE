package proyecto.iso2.dominio.gestores;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServicioEntregaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private RepartidorDAO repartidorDAO;

    @Autowired
    private PagoDAO pagoDAO;

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private ServicioEntregaDAO servicioEntregaDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private MockHttpSession session;


    @BeforeEach
    public void setUp() {
        // Configuración manual de MockMvc como respaldo
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        // Inicializamos el TransactionTemplate
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Limpiamos la base de datos respetando las dependencias
        transactionTemplate.execute(status -> {
            // Eliminar en orden para evitar violaciones de claves foráneas
            entityManager.createNativeQuery("DELETE FROM servicio_entrega").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            // Desvincular PAGO_ID en pedido para evitar violación de clave foránea
            entityManager.createNativeQuery("UPDATE pedido SET pago_id = NULL").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pago").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });

        // Inicializamos la sesión
        session = new MockHttpSession();
    }


    @Test
    public void testFinalizarPedidoSinClienteEnSesion() throws Exception {
        // Preparar datos de prueba
        Direccion direccion = transactionTemplate.execute(status -> {
            Direccion d = new Direccion();
            d.setCodigoPostal(12300);
            d.setCalle("Calle Principal");
            d.setNumero(1);
            d.setMunicipio("Madrid");
            return direccionDAO.save(d);
        });

        Pedido pedido = transactionTemplate.execute(status -> {
            Pedido p = new Pedido();
            p.setEstado(EstadoPedido.PEDIDO);
            p.setFecha(LocalDateTime.now());
            return pedidoDAO.save(p);
        });

        // Ejecutar la solicitud
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", direccion.getId().toString())
                        .param("pedidoId", pedido.getId().toString())
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }


    @Test
    public void testFinalizarPedidoConCarritoVacio() throws Exception {
        // Preparar datos de prueba
        Cliente cliente = transactionTemplate.execute(status -> {
            Cliente c = new Cliente();
            c.setEmail("cliente@ejemplo.com");
            c.setNombre("Cliente A");
            return clienteDAO.save(c);
        });

        Direccion direccion = transactionTemplate.execute(status -> {
            Direccion d = new Direccion();
            d.setCodigoPostal(12300);
            d.setCalle("Calle Principal");
            d.setNumero(1);
            d.setMunicipio("Madrid");
            return direccionDAO.save(d);
        });

        Restaurante restaurante = transactionTemplate.execute(status -> {
            Direccion direccionRestaurante = new Direccion();
            direccionRestaurante.setCodigoPostal(12300);
            direccionRestaurante.setCalle("Calle Restaurante");
            direccionRestaurante.setNumero(10);
            direccionRestaurante.setMunicipio("Madrid");
            direccionRestaurante = direccionDAO.save(direccionRestaurante);
            Restaurante r = new Restaurante();
            r.setNombre("Restaurante A");
            r.setCif("B12345678");
            r.setDireccion(direccionRestaurante);
            return restauranteDAO.save(r);
        });

        Pedido pedido = transactionTemplate.execute(status -> {
            Pedido p = new Pedido();
            p.setCliente(cliente);
            p.setRestaurante(restaurante);
            p.setEstado(EstadoPedido.PEDIDO);
            p.setFecha(LocalDateTime.now());
            return pedidoDAO.save(p);
        });

        session.setAttribute("cliente", cliente);

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", direccion.getId().toString())
                        .param("pedidoId", pedido.getId().toString())
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "El carrito está vacío"));
    }


    @Test
    public void testFinalizarPedidoSinRepartidorDisponible() throws Exception {
        // Preparar datos de prueba
        Cliente cliente = transactionTemplate.execute(status -> {
            Cliente c = new Cliente();
            c.setEmail("cliente@ejemplo.com");
            c.setNombre("Cliente A");
            return clienteDAO.save(c);
        });

        Direccion direccion = transactionTemplate.execute(status -> {
            Direccion d = new Direccion();
            d.setCodigoPostal(12300);
            d.setCalle("Calle Principal");
            d.setNumero(1);
            d.setMunicipio("Madrid");
            return direccionDAO.save(d);
        });

        Restaurante restaurante = transactionTemplate.execute(status -> {
            Direccion direccionRestaurante = new Direccion();
            direccionRestaurante.setCodigoPostal(12300);
            direccionRestaurante.setCalle("Calle Restaurante");
            direccionRestaurante.setNumero(10);
            direccionRestaurante.setMunicipio("Madrid");
            direccionRestaurante = direccionDAO.save(direccionRestaurante);
            Restaurante r = new Restaurante();
            r.setNombre("Restaurante A");
            r.setCif("B12345678");
            r.setDireccion(direccionRestaurante);
            return restauranteDAO.save(r);
        });

        CartaMenu carta = transactionTemplate.execute(status -> {
            CartaMenu cm = new CartaMenu();
            cm.setNombre("Carta Principal");
            cm.setRestaurante(restaurante);
            return cartaMenuDAO.save(cm);
        });

        ItemMenu item = transactionTemplate.execute(status -> {
            ItemMenu i = new ItemMenu();
            i.setNombre("Pizza");
            i.setPrecio(10.0);
            i.setTipo("Comida");
            i.setCartaMenu(carta);
            return itemMenuDAO.save(i);
        });

        Pedido pedido = transactionTemplate.execute(status -> {
            Pedido p = new Pedido();
            p.setCliente(cliente);
            p.setRestaurante(restaurante);
            p.setEstado(EstadoPedido.PEDIDO);
            p.setFecha(LocalDateTime.now());
            return pedidoDAO.save(p);
        });

        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(item.getId(), 2);
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", direccion.getId().toString())
                        .param("pedidoId", pedido.getId().toString())
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "No hay repartidores disponibles en esta zona."));
    }
}