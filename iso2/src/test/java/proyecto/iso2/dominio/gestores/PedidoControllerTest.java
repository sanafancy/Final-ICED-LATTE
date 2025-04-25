package proyecto.iso2.dominio.gestores;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;
import org.springframework.mock.web.MockHttpSession;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PedidoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PedidoDAO pedidoDAO;

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private RestauranteDAO restauranteDAO;

    @Mock
    private ItemMenuDAO itemMenuDAO;

    @Mock
    private DireccionDAO direccionDAO;

    @Mock
    private CartaMenuDAO cartaMenuDAO;

    @InjectMocks
    private PedidoController pedidoController;

    private MockHttpSession session;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Configurar Thymeleaf para las pruebas (necesario para los métodos que renderizan vistas)
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setCharacterEncoding("UTF-8");

        // Configurar MockMvc con el controlador y el ViewResolver
        mockMvc = MockMvcBuilders.standaloneSetup(pedidoController)
                .setViewResolvers(viewResolver)
                .build();

        // Inicializar la sesión
        session = new MockHttpSession();

        // Inicializar ObjectMapper para manejar JSON
        objectMapper = new ObjectMapper();
    }

    // Métodos auxiliares para crear entidades
    private Cliente crearCliente(String nombre, String apellidos, String email, String pass, String dni) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        setField(cliente, "idUsuario", 1L);
        cliente.setDirecciones(new ArrayList<>());
        cliente.setFavoritos(new ArrayList<>());
        return cliente;
    }

    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, Direccion direccion) {
        Restaurante restaurante = new Restaurante(email, pass, nombre, cif, direccion);
        setField(restaurante, "idUsuario", 2L);
        return restaurante;
    }

    private Direccion crearDireccion() {
        return new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
    }

    private ItemMenu crearItemMenu(Long id, double precio, CartaMenu cartaMenu) {
        ItemMenu item = new ItemMenu();
        setField(item, "id", id);
        item.setPrecio(precio);
        item.setCartaMenu(cartaMenu);
        item.setNombre("Item " + id);
        return item;
    }

    private CartaMenu crearCartaMenu(Restaurante restaurante) {
        CartaMenu carta = new CartaMenu();
        carta.setRestaurante(restaurante);
        return carta;
    }

    // Método auxiliar para establecer el ID usando reflexión
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field;
            if (fieldName.equals("idUsuario")) {
                field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            } else {
                field = target.getClass().getDeclaredField(fieldName);
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el campo " + fieldName, e);
        }
    }

    // Pruebas para el método verMenus (GET /pedido/verMenus)
    @Test
    public void testVerMenus_SinCliente() throws Exception {
        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/pedido/verMenus")
                        .param("restauranteId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testVerMenus_RestauranteNoExistente() throws Exception {
        // Datos de prueba
        Long restauranteId = 2L;
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        session.setAttribute("cliente", cliente);

        // Configurar los mocks
        when(restauranteDAO.findById(restauranteId)).thenReturn(Optional.empty());

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/pedido/verMenus")
                        .param("restauranteId", String.valueOf(restauranteId))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testVerMenus_Exitoso() throws Exception {
        // Datos de prueba
        Long restauranteId = 2L;
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        List<CartaMenu> cartas = Arrays.asList(crearCartaMenu(restaurante));
        session.setAttribute("cliente", cliente);

        // Configurar los mocks
        when(restauranteDAO.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(cartaMenuDAO.findByRestaurante(restaurante)).thenReturn(cartas);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/pedido/verMenus")
                        .param("restauranteId", String.valueOf(restauranteId))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attribute("cliente", cliente))
                .andExpect(model().attribute("restaurante", restaurante))
                .andExpect(model().attribute("cartas", cartas));
    }

    // Pruebas para el método confirmarPedido (GET /pedido/confirmarPedido)
    @Test
    public void testConfirmarPedido_SinCliente() throws Exception {
        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testConfirmarPedido_CarritoVacio() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Long restauranteId = 2L;
        session.setAttribute("cliente", cliente);
        session.setAttribute("restauranteId", restauranteId);

        // Configurar los mocks
        when(clienteDAO.findById(cliente.getIdUsuario())).thenReturn(Optional.of(cliente));

        // Ejecutar la solicitud GET con carrito vacío
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verMenus?restauranteId=" + restauranteId));
    }

    @Test
    public void testConfirmarPedido_Exitoso() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        Pedido pedido = new Pedido(cliente, restaurante);
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del item 1
        carrito.put(2L, 1); // 1 unidad del item 2
        Direccion direccion = crearDireccion();
        Long direccionId = 3L;
        String metodoPago = MetodoPago.PAYPAL.name();

        session.setAttribute("cliente", cliente);
        session.setAttribute("pedido", pedido);
        session.setAttribute("carrito", carrito);
        session.setAttribute("direccionId", direccionId);
        session.setAttribute("metodoPago", metodoPago);
        session.setAttribute("restauranteId", restaurante.getIdUsuario());

        ItemMenu item1 = crearItemMenu(1L, 10.0, new CartaMenu());
        ItemMenu item2 = crearItemMenu(2L, 15.0, new CartaMenu());
        when(clienteDAO.findById(cliente.getIdUsuario())).thenReturn(Optional.of(cliente));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item1));
        when(itemMenuDAO.findById(2L)).thenReturn(Optional.of(item2));
        when(direccionDAO.findById(direccionId)).thenReturn(Optional.of(direccion));

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("cliente", cliente))
                .andExpect(model().attribute("total", String.format("%.2f", 35.0))) // 2*10 + 1*15
                .andExpect(model().attribute("itemsPedido", Arrays.asList(item1, item2)))
                .andExpect(model().attribute("direccion", direccion))
                .andExpect(model().attribute("metodoPago", metodoPago));
    }

    // Pruebas para el método procesarPedido (POST /pedido/confirmarPedido)
    @Test
    public void testProcesarPedido_SinCliente() throws Exception {
        // Datos de prueba
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2);
        String carritoJson = objectMapper.writeValueAsString(carrito);
        Long direccionId = 3L;

        // Ejecutar la solicitud POST sin cliente en la sesión
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.PAYPAL.name())
                        .param("carrito", carritoJson)
                        .param("direccionId", direccionId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testProcesarPedido_CarritoVacio() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        session.setAttribute("cliente", cliente);
        Long direccionId = 3L;
        Direccion direccion = crearDireccion();

        // Configurar los mocks
        when(direccionDAO.findById(direccionId)).thenReturn(Optional.of(direccion));

        // Ejecutar la solicitud POST con carrito vacío
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.name())
                        .param("carrito", "{}")
                        .param("direccionId", direccionId.toString())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attribute("error", "El carrito está vacío"));
    }

    @Test
    public void testProcesarPedido_Exitoso() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del item 1
        String carritoJson = objectMapper.writeValueAsString(carrito);
        Long direccionId = 3L;
        Direccion direccion = crearDireccion();
        session.setAttribute("cliente", cliente);

        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        CartaMenu carta = crearCartaMenu(restaurante);
        ItemMenu item1 = crearItemMenu(1L, 10.0, carta);

        // Configurar los mocks
        when(direccionDAO.findById(direccionId)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item1));
        when(pedidoDAO.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            setField(pedido, "id", 1L);
            return pedido;
        });

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.CREDIT_CARD.name())
                        .param("carrito", carritoJson)
                        .param("direccionId", direccionId.toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedido/confirmarPedido"));

        // Verificar que se guardó el pedido
        verify(pedidoDAO, times(1)).save(any(Pedido.class));
    }
}