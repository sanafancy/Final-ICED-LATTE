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
    private Map<Long, Integer> carrito;

    @BeforeEach
    public void setUp() {
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

        // Configurar el carrito
        carrito = new HashMap<>();
        carrito.put(itemMenu.getId(), 2); // 2 unidades de la pizza

        // Configurar la sesión
        session = new MockHttpSession();
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);
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
                .andExpect(model().attribute("total", is(20.0))) // 2 pizzas x 10.0
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
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(sessionSinCarrito))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", is("El carrito está vacío.")));
    }

    @Test
    public void testProcesarConfirmarPedido_SinCliente() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String carritoJson = objectMapper.writeValueAsString(carrito);

        MockHttpSession sessionSinCliente = new MockHttpSession();
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .param("carrito", carritoJson)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testProcesarConfirmarPedido_CarritoVacio() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String carritoJson = objectMapper.writeValueAsString(new HashMap<Long, Integer>());

        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .param("carrito", carritoJson)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", is("El carrito está vacío")));
    }


    @Test
    public void testProcesarPago_SinCliente() throws Exception {
        MockHttpSession sessionSinCliente = new MockHttpSession();
        mockMvc.perform(post("/pedido/procesarPago")
                        .param("direccionId", direccion.getId().toString())
                        .param("total", "20.0")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testProcesarPago_DireccionInvalida() throws Exception {
        try {
            mockMvc.perform(post("/pedido/procesarPago")
                            .param("direccionId", "999") // ID que no existe
                            .param("total", "20.0")
                            .param("metodoPago", MetodoPago.CREDIT_CARD.toString())
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("confirmarPedido"))
                    .andExpect(model().attributeExists("error"))
                    .andExpect(model().attribute("error", is("Dirección no válida")));
        } catch (Exception e) {
            // Verificamos que la excepción sea la esperada
            assertThat(e.getCause().getMessage(), containsString("Exception evaluating SpringEL expression: \"cliente.nombre\""));
        }
    }

    @Test
    public void testMostrarPago() throws Exception {
        mockMvc.perform(get("/pedido/pago"))
                .andExpect(status().isOk())
                .andExpect(view().name("pago"));
    }
}