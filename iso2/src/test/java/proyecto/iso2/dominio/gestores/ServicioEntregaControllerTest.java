package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ServicioEntregaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ServicioEntregaDAO servicioEntregaDAO;

    @Mock
    private PedidoDAO pedidoDAO;

    @Mock
    private DireccionDAO direccionDAO;

    @Mock
    private RepartidorDAO repartidorDAO;

    @Mock
    private PagoDAO pagoDAO;

    @Mock
    private ItemMenuDAO itemMenuDAO;

    @InjectMocks
    private ServicioEntregaController servicioEntregaController;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks y resetearlos
        MockitoAnnotations.openMocks(this);
        reset(servicioEntregaDAO, pedidoDAO, direccionDAO, repartidorDAO, pagoDAO, itemMenuDAO);

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
        mockMvc = MockMvcBuilders.standaloneSetup(servicioEntregaController)
                .setViewResolvers(viewResolver)
                .build();

        // Inicializar la sesión
        session = new MockHttpSession();
    }

    // Métodos auxiliares para crear entidades
    private Cliente crearCliente() {
        Cliente cliente = new Cliente("cliente@email.com", "pass123", "Juan", "Pérez", "12345678A");
        setIdUsuario(cliente, 1L);
        return cliente;
    }

    private Pedido crearPedido() {
        Pedido pedido = new Pedido();
        trySetId(pedido, 1L);
        return pedido;
    }

    private Direccion crearDireccion() {
        Direccion direccion = new Direccion("Calle Falsa", 123, "Piso 1", 28001, "Madrid");
        trySetId(direccion, 1L);
        return direccion;
    }

    private Repartidor crearRepartidor() {
        Repartidor repartidor = new Repartidor("repartidor@email.com", "pass123", "Ana", "García", "87654321B", 90);
        setIdUsuario(repartidor, 2L);
        return repartidor;
    }

    private ItemMenu crearItemMenu(Long id, double precio) {
        ItemMenu item = new ItemMenu();
        trySetId(item, id);
        item.setPrecio(precio);
        return item;
    }

    private Pago crearPago(Pedido pedido, MetodoPago metodoPago) {
        return new Pago(metodoPago, pedido, LocalDateTime.now());
    }

    // Método auxiliar para establecer el ID usando setters (Usuario)
    private void setIdUsuario(Object target, Long id) {
        try {
            Method setIdUsuario = target.getClass().getSuperclass().getDeclaredMethod("setIdUsuario", Long.class);
            setIdUsuario.invoke(target, id);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error al establecer el ID usando setIdUsuario", e);
        }
    }

    // Método auxiliar para intentar establecer el ID en otras entidades
    private void trySetId(Object target, Long id) {
        String[] possibleSetters = {"setId", "setIdPedido", "setIdDireccion", "setIdItem", "setIdItemMenu"};
        for (String setter : possibleSetters) {
            try {
                Method setId = target.getClass().getDeclaredMethod(setter, Long.class);
                setId.invoke(target, id);
                return;
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                // Continúa intentando otros setters
            }
        }
        // Si no se encuentra ningún setter, no lanzar excepción para evitar fallos innecesarios
        System.out.println("No se encontró setter de ID para " + target.getClass().getSimpleName());
    }

    // Pruebas para POST /pedido/finalizar
    @Test
    public void testFinalizarPedido_SinCliente() throws Exception {
        // Ejecutar la solicitud POST sin cliente en la sesión
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testFinalizarPedido_PedidoNoValido() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        session.setAttribute("cliente", cliente);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "Pedido o dirección no válidos"));
    }

    @Test
    public void testFinalizarPedido_DireccionNoValida() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        session.setAttribute("cliente", cliente);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "Pedido o dirección no válidos"));
    }

    @Test
    public void testFinalizarPedido_MetodoPagoNoValido() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        Direccion direccion = crearDireccion();
        session.setAttribute("cliente", cliente);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));

        // Ejecutar la solicitud POST con método de pago inválido
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "INVALIDO")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "Método de pago no válido"));
    }

    @Test
    public void testFinalizarPedido_CarritoVacio() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        Direccion direccion = crearDireccion();
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", new HashMap<Long, Integer>()); // Carrito vacío

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "El carrito está vacío"));
    }

    @Test
    public void testFinalizarPedido_SinRepartidoresConEficiencia() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        Direccion direccion = crearDireccion();
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del ítem 1
        ItemMenu item = crearItemMenu(1L, 10.0); // Precio 10.0
        Repartidor repartidor = crearRepartidor();
        repartidor.setEficiencia(null); // Sin eficiencia
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(repartidorDAO.findAll()).thenReturn(Arrays.asList(repartidor));
        when(pagoDAO.save(any(Pago.class))).thenReturn(crearPago(pedido, MetodoPago.CREDIT_CARD));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "No hay repartidores disponibles con eficiencia definida"));
    }

    @Test
    public void testFinalizarPedido_Exitoso() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        Direccion direccion = crearDireccion();
        Repartidor repartidor = crearRepartidor();
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del ítem 1
        ItemMenu item = crearItemMenu(1L, 10.0); // Precio 10.0
        Pago pago = crearPago(pedido, MetodoPago.CREDIT_CARD);
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(repartidorDAO.findAll()).thenReturn(Arrays.asList(repartidor));
        when(pagoDAO.save(any(Pago.class))).thenReturn(pago);
        when(pedidoDAO.save(any(Pedido.class))).thenReturn(pedido);
        when(servicioEntregaDAO.save(any(ServicioEntrega.class))).thenReturn(new ServicioEntrega());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pagoExitoso"))
                .andExpect(model().attribute("success", "Pedido confirmado y pago realizado con éxito."));

        // Verificar interacciones
        verify(pagoDAO, times(1)).save(any(Pago.class));
        verify(pedidoDAO, times(1)).save(any(Pedido.class));
        verify(servicioEntregaDAO, times(1)).save(any(ServicioEntrega.class));
        verify(repartidorDAO, times(1)).findAll();

        // Verificar limpieza de sesión
        assertThat(session.getAttribute("carrito"), is(nullValue()));
        assertThat(session.getAttribute("pedido"), is(nullValue()));
        assertThat(session.getAttribute("direccionId"), is(nullValue()));
        assertThat(session.getAttribute("metodoPago"), is(nullValue()));
        assertThat(session.getAttribute("restauranteId"), is(nullValue()));
    }

    @Test
    public void testFinalizarPedido_ErrorProcesamiento() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Pedido pedido = crearPedido();
        Direccion direccion = crearDireccion();
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2); // 2 unidades del ítem 1
        ItemMenu item = crearItemMenu(1L, 10.0); // Precio 10.0
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        // Configurar mocks
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(pagoDAO.save(any(Pago.class))).thenThrow(new RuntimeException("Error de base de datos"));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", org.hamcrest.Matchers.startsWith("Error al procesar el pago")));
    }
}