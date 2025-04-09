package proyecto.iso2.dominio.gestores;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;
import org.springframework.mock.web.MockHttpSession;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private MockHttpSession session;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        pedidoDAO = mock(PedidoDAO.class);
        clienteDAO = mock(ClienteDAO.class);
        restauranteDAO = mock(RestauranteDAO.class);
        itemMenuDAO = mock(ItemMenuDAO.class);
        direccionDAO = mock(DireccionDAO.class);
        cartaMenuDAO = mock(CartaMenuDAO.class);

        // Crear una instancia del controlador
        PedidoController controller = new PedidoController();

        // Usar reflexión para inyectar los mocks en los campos privados
        injectMock(controller, "pedidoDAO", pedidoDAO);
        injectMock(controller, "clienteDAO", clienteDAO);
        injectMock(controller, "restauranteDAO", restauranteDAO);
        injectMock(controller, "itemMenuDAO", itemMenuDAO);
        injectMock(controller, "direccionDAO", direccionDAO);
        injectMock(controller, "cartaMenuDAO", cartaMenuDAO);

        // Configurar Thymeleaf para las pruebas
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();

        // Inicializar la sesión
        session = new MockHttpSession();

        // Inicializar ObjectMapper para manejar JSON
        objectMapper = new ObjectMapper();
    }

    // Método auxiliar para inyectar un mock en un campo privado usando reflexión
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // Métodos auxiliares para crear entidades
    private Cliente crearCliente(String nombre, String apellidos, String email, String pass, String dni) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        cliente.setIdUsuario(1L);
        cliente.setDirecciones(new ArrayList<>());
        cliente.setFavoritos(new ArrayList<>());
        return cliente;
    }

    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, Direccion direccion) {
        Restaurante restaurante = new Restaurante(email, pass, nombre, cif, direccion);
        restaurante.setIdUsuario(2L);
        return restaurante;
    }

    private Direccion crearDireccion() {
        return new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
    }

    private ItemMenu crearItemMenu(Long id, double precio, CartaMenu cartaMenu) {
        ItemMenu item = new ItemMenu();
        item.setId(id);
        item.setPrecio(precio);
        item.setCartaMenu(cartaMenu);
        return item;
    }

    private CartaMenu crearCartaMenu(Restaurante restaurante) {
        CartaMenu carta = new CartaMenu();
        carta.setRestaurante(restaurante);
        return carta;
    }

   /* // Pruebas para el método crearPedido (POST /pedido/crear)
    @Test
    public void testCrearPedido() throws Exception {
        // Datos de prueba
        Long clienteId = 1L;
        Long restauranteId = 2L;
        Long direccionId = 3L;
        MetodoPago metodoPago = MetodoPago.TARJETA;
        List<Long> itemIds = Arrays.asList(1L, 2L);

        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        Direccion direccion = crearDireccion();
        List<ItemMenu> items = Arrays.asList(
                crearItemMenu(1L, 10.0, new CartaMenu()),
                crearItemMenu(2L, 15.0, new CartaMenu())
        );

        // Configurar los mocks
        when(clienteDAO.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(restauranteDAO.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(direccionDAO.findById(direccionId)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findAllById(itemIds)).thenReturn(items);
        when(pedidoDAO.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar la solicitud POST
        Pedido pedido = new PedidoController()
                .crearPedido(clienteId, restauranteId, direccionId, metodoPago, itemIds);

        // Verificar que el pedido se creó correctamente
        assertEquals(cliente, pedido.getCliente());
        assertEquals(restaurante, pedido.getRestaurante());
        assertEquals(EstadoPedido.PEDIDO, pedido.getEstado());
        assertEquals(metodoPago, pedido.getMetodoPago());
        assertEquals(items, pedido.getItems());
        verify(pedidoDAO, times(1)).save(any(Pedido.class));
    }

    // Pruebas para el método obtenerPedidosRestaurante (GET /pedido/restaurante/{restauranteId})
    @Test
    public void testObtenerPedidosRestaurante_RestauranteExistente() {
        // Datos de prueba
        Long restauranteId = 2L;
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        List<Pedido> pedidos = Arrays.asList(new Pedido(), new Pedido());

        // Configurar los mocks
        when(restauranteDAO.findById(restauranteId)).thenReturn(Optional.of(restaurante));
        when(pedidoDAO.findByRestaurante(restaurante)).thenReturn(pedidos);

        // Ejecutar el método
        List<Pedido> resultado = new PedidoController()
                .obtenerPedidosRestaurante(restauranteId);

        // Verificar el resultado
        assertEquals(pedidos, resultado);
        verify(pedidoDAO, times(1)).findByRestaurante(restaurante);
    }

    @Test
    public void testObtenerPedidosRestaurante_RestauranteNoExistente() {
        // Datos de prueba
        Long restauranteId = 2L;

        // Configurar los mocks
        when(restauranteDAO.findById(restauranteId)).thenReturn(Optional.empty());

        // Ejecutar el método
        List<Pedido> resultado = new PedidoController()
                .obtenerPedidosRestaurante(restauranteId);

        // Verificar el resultado
        assertEquals(0, resultado.size());
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
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud GET con carrito vacío
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attribute("error", "El carrito está vacío."));
    }

    @Test
    public void testConfirmarPedido_Exitoso() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del item 1
        carrito.put(2L, 1); // 1 unidad del item 2
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        ItemMenu item1 = crearItemMenu(1L, 10.0, new CartaMenu());
        ItemMenu item2 = crearItemMenu(2L, 15.0, new CartaMenu());
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item1));
        when(itemMenuDAO.findById(2L)).thenReturn(Optional.of(item2));

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/pedido/confirmarPedido")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("cliente", cliente))
                .andExpect(model().attribute("total", 35.0)) // 2*10 + 1*15
                .andExpect(model().attribute("itemsPedido", Arrays.asList(item1, item2)));
    }

    // Pruebas para el método procesarPedido (POST /pedido/confirmarPedido)
    @Test
    public void testProcesarPedido_SinCliente() throws Exception {
        // Ejecutar la solicitud POST sin cliente en la sesión
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.TARJETA.name())
                        .param("carrito", "{}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testProcesarPedido_CarritoVacio() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud POST con carrito vacío
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.TARJETA.name())
                        .param("carrito", "{}")
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
        session.setAttribute("cliente", cliente);

        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        CartaMenu carta = crearCartaMenu(restaurante);
        ItemMenu item1 = crearItemMenu(1L, 10.0, carta);
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item1));
        when(pedidoDAO.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/confirmarPedido")
                        .param("metodoPago", MetodoPago.TARJETA.name())
                        .param("carrito", carritoJson)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedido/confirmarPedido"));

        // Verificar que se guardó el pedido
        verify(pedidoDAO, times(1)).save(any(Pedido.class));
    }*/
}