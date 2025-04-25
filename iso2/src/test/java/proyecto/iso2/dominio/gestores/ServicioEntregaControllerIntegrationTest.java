package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ServicioEntregaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServicioEntregaDAO servicioEntregaDAO;

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private ClienteDAO clienteDAO;

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

    private MockHttpSession session;
    private Cliente cliente;
    private Restaurante restaurante;
    private Direccion direccion;
    private CartaMenu cartaMenu;
    private ItemMenu itemMenu;
    private Pedido pedido;
    private Repartidor repartidor;
    private Map<Long, Integer> carrito;

    @BeforeEach
    public void setUp() {
        // Limpiar la base de datos en orden inverso para respetar las restricciones de clave foránea
        pagoDAO.deleteAllInBatch(); // Elimina pagos primero (depende de pedido)
        servicioEntregaDAO.deleteAllInBatch(); // Elimina servicios de entrega (depende de pedido)
        pedidoDAO.deleteAllInBatch(); // Ahora elimina pedidos
        itemMenuDAO.deleteAllInBatch();
        cartaMenuDAO.deleteAllInBatch();
        direccionDAO.deleteAllInBatch();
        repartidorDAO.deleteAllInBatch();
        clienteDAO.deleteAllInBatch();
        restauranteDAO.deleteAllInBatch();

        // Crear un cliente
        cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        cliente = clienteDAO.save(cliente);

        // Crear un restaurante
        restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante = restauranteDAO.save(restaurante);

        // Crear una dirección
        direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        direccion = direccionDAO.save(direccion);

        // Crear una carta de menú
        cartaMenu = new CartaMenu();
        cartaMenu.setNombre("Menú Principal");
        cartaMenu.setRestaurante(restaurante);
        cartaMenu = cartaMenuDAO.save(cartaMenu);

        // Crear un ítem de menú
        itemMenu = new ItemMenu();
        itemMenu.setNombre("Pizza");
        itemMenu.setPrecio(10.0);
        itemMenu.setTipo("PLATO");
        itemMenu.setCartaMenu(cartaMenu);
        itemMenu = itemMenuDAO.save(itemMenu);

        // Crear un pedido
        pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setEstado(EstadoPedido.PEDIDO);
        pedido.setFecha(LocalDateTime.now());
        pedido.setMetodoPago(MetodoPago.CREDIT_CARD);
        pedido.setTotal(20.0);
        pedido = pedidoDAO.save(pedido);

        // Crear un repartidor
        repartidor = new Repartidor();
        repartidor.setEmail("repartidor@ejemplo.com");
        repartidor.setPass("pass123");
        repartidor.setNombre("Repartidor A");
        repartidor.setApellidos("Apellido A");
        repartidor.setNif("87654321B");
        repartidor.setEficiencia(90);
        repartidor = repartidorDAO.save(repartidor);

        // Configurar el carrito
        carrito = new HashMap<>();
        carrito.put(itemMenu.getId(), 2); // 2 unidades de la pizza

        // Configurar la sesión
        session = new MockHttpSession();
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);
        session.setAttribute("pedido", pedido);
        session.setAttribute("direccionId", direccion.getId());
        session.setAttribute("metodoPago", MetodoPago.CREDIT_CARD.toString());
        session.setAttribute("restauranteId", restaurante.getIdUsuario());
    }

    @Test
    public void testFinalizarPedido_SinCliente() throws Exception {
        MockHttpSession sessionSinCliente = new MockHttpSession();
        sessionSinCliente.setAttribute("carrito", carrito);

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", direccion.getId().toString())
                        .param("pedidoId", pedido.getId().toString())
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testFinalizarPedido_SinRepartidoresConEficiencia() throws Exception {
        // Eliminar repartidor existente
        repartidorDAO.deleteAllInBatch();

        // Crear repartidor sin eficiencia
        Repartidor repartidorSinEficiencia = new Repartidor();
        repartidorSinEficiencia.setEmail("repartidor2@ejemplo.com");
        repartidorSinEficiencia.setPass("pass123");
        repartidorSinEficiencia.setNombre("Repartidor B");
        repartidorSinEficiencia.setApellidos("Apellido B");
        repartidorSinEficiencia.setNif("98765432B");
        repartidorSinEficiencia.setEficiencia(null);
        repartidorDAO.save(repartidorSinEficiencia);

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", direccion.getId().toString())
                        .param("pedidoId", pedido.getId().toString())
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", is("No hay repartidores disponibles con eficiencia definida")));
    }

}