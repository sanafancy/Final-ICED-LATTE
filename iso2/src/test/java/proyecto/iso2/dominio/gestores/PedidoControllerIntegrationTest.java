package proyecto.iso2.dominio.gestores;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    private MockHttpSession session;
    private Cliente cliente;
    private Restaurante restaurante;
    private Direccion direccion;
    private CartaMenu cartaMenu;
    private ItemMenu itemMenu;
    private Pedido pedido;
    private Map<Long, Integer> carrito;

    @BeforeEach
    public void setUp() {
        // Limpiar la base de datos
        pedidoDAO.deleteAll();
        itemMenuDAO.deleteAll();
        cartaMenuDAO.deleteAll();
        direccionDAO.deleteAll();
        clienteDAO.deleteAll();
        restauranteDAO.deleteAll();

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
    public void testConfirmarPedido_ConClienteYCarrito() throws Exception {
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attributeExists("cliente"))
                .andExpect(model().attributeExists("itemsPedido"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", is(String.format("%.2f", 20.0)))) // 2 pizzas x 10.0
                .andExpect(model().attribute("itemsPedido", hasSize(1)))
                .andExpect(model().attribute("itemsPedido", hasItem(
                        hasProperty("nombre", is("Pizza"))
                )));
    }

    @Test
    public void testConfirmarPedido_SinCliente() throws Exception {
        MockHttpSession sessionSinCliente = new MockHttpSession();
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testConfirmarPedido_SinCarrito() throws Exception {
        MockHttpSession sessionSinCarrito = new MockHttpSession();
        sessionSinCarrito.setAttribute("cliente", cliente);
        sessionSinCarrito.setAttribute("pedido", pedido);
        sessionSinCarrito.setAttribute("direccionId", direccion.getId());
        sessionSinCarrito.setAttribute("metodoPago", MetodoPago.CREDIT_CARD.toString());
        sessionSinCarrito.setAttribute("restauranteId", restaurante.getIdUsuario());
        // No se establece carrito

        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(sessionSinCarrito))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verMenus?restauranteId=" + restaurante.getIdUsuario()));
    }

    @Test
    public void testProcesarConfirmarPedido_SinCliente() throws Exception {
        MockHttpSession sessionSinCliente = new MockHttpSession();
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .param("carrito", "{\"1\":2}") // Formato JSON válido
                        .param("direccionId", direccion.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    /*@Test
    public void testProcesarConfirmarPedido_CarritoVacio() throws Exception {
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .param("carrito", "{}") // JSON vacío válido
                        .param("direccionId", direccion.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", is("El carrito está vacío")));
    }*/
}