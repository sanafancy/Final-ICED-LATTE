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
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;
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
        MockitoAnnotations.openMocks(this);
        reset(servicioEntregaDAO, pedidoDAO, direccionDAO, repartidorDAO, pagoDAO, itemMenuDAO);

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

        mockMvc = MockMvcBuilders.standaloneSetup(servicioEntregaController)
                .setViewResolvers(viewResolver)
                .build();

        session = new MockHttpSession();
    }

    private Cliente crearCliente() {
        Cliente cliente = new Cliente("cliente@email.com", "pass123", "Juan", "Pérez", "12345678A");
        setIdUsuario(cliente, 1L);
        return cliente;
    }

    private Restaurante crearRestaurante(int codigoPostal) {
        Direccion direccion = new Direccion("Calle Restaurante", 123, "Local 1", codigoPostal, "Madrid");
        trySetId(direccion, 2L);
        Restaurante restaurante = new Restaurante("restaurante@email.com", "pass123", "Restaurante A", "CIF123", direccion);
        setIdUsuario(restaurante, 3L);
        return restaurante;
    }

    private Pedido crearPedido(Restaurante restaurante) {
        Pedido pedido = new Pedido();
        trySetId(pedido, 1L);
        pedido.setRestaurante(restaurante);
        return pedido;
    }

    private Direccion crearDireccion(int codigoPostal) {
        Direccion direccion = new Direccion("Calle Falsa", 123, "Piso 1", codigoPostal, "Madrid");
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

    private void setIdUsuario(Object target, Long id) {
        try {
            Method setIdUsuario = target.getClass().getSuperclass().getDeclaredMethod("setIdUsuario", Long.class);
            setIdUsuario.invoke(target, id);
        } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error al establecer el ID usando setIdUsuario", e);
        }
    }

    private void trySetId(Object target, Long id) {
        String[] possibleSetters = {"setId", "setIdPedido", "setIdDireccion", "setIdItem", "setIdItemMenu"};
        for (String setter : possibleSetters) {
            try {
                Method setId = target.getClass().getDeclaredMethod(setter, Long.class);
                setId.invoke(target, id);
                return;
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            }
        }
        System.out.println("No se encontró setter de ID para " + target.getClass().getSimpleName());
    }

    @Test
    public void testFinalizarPedido_SinCliente() throws Exception {
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
        Cliente cliente = crearCliente();
        session.setAttribute("cliente", cliente);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.empty());

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
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        session.setAttribute("cliente", cliente);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.empty());

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
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        Direccion direccion = crearDireccion(12300); // Usar código postal válido
        session.setAttribute("cliente", cliente);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));

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
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        Direccion direccion = crearDireccion(12300); // Usar código postal válido
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", new HashMap<Long, Integer>());

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));

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
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        Direccion direccion = crearDireccion(12300); // Usar código postal válido
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2);
        ItemMenu item = crearItemMenu(1L, 10.0);
        Pago pago = crearPago(pedido, MetodoPago.CREDIT_CARD);
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(servicioEntregaDAO.findByDireccionAndFechaEntregaIsNull(direccion)).thenReturn(new ArrayList<>());
        // Devolver lista vacía para simular que no hay repartidores disponibles
        when(repartidorDAO.findByCodigoPostalOrderByEficienciaAsc(ArgumentMatchers.any(CodigoPostal.class))).thenReturn(new ArrayList<>());
        when(pagoDAO.save(ArgumentMatchers.any(Pago.class))).thenReturn(pago);
        when(pedidoDAO.save(ArgumentMatchers.any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("confirmarPedido"))
                .andExpect(model().attribute("error", "No hay repartidores disponibles en esta zona."));
    }

    @Test
    public void testFinalizarPedido_Exitoso() throws Exception {
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        Direccion direccion = crearDireccion(12300); // Usar código postal válido
        Repartidor repartidor = crearRepartidor();
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2);
        ItemMenu item = crearItemMenu(1L, 10.0);
        Pago pago = crearPago(pedido, MetodoPago.CREDIT_CARD);
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(servicioEntregaDAO.findByDireccionAndFechaEntregaIsNull(direccion)).thenReturn(new ArrayList<>());
        when(repartidorDAO.findByCodigoPostalOrderByEficienciaAsc(ArgumentMatchers.any(CodigoPostal.class))).thenReturn(Arrays.asList(repartidor));
        when(pagoDAO.save(ArgumentMatchers.any(Pago.class))).thenReturn(pago);
        when(pedidoDAO.save(ArgumentMatchers.any(Pedido.class))).thenReturn(pedido);
        when(repartidorDAO.save(ArgumentMatchers.any(Repartidor.class))).thenReturn(repartidor);
        when(servicioEntregaDAO.save(ArgumentMatchers.any(ServicioEntrega.class))).thenReturn(new ServicioEntrega());

        mockMvc.perform(post("/pedido/finalizar")
                        .param("direccionId", "1")
                        .param("pedidoId", "1")
                        .param("metodoPago", "CREDIT_CARD")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("pagoExitoso"))
                .andExpect(model().attribute("success", "Pedido confirmado y pago realizado con éxito."));

        verify(pagoDAO, times(1)).save(ArgumentMatchers.any(Pago.class));
        verify(pedidoDAO, times(1)).save(ArgumentMatchers.any(Pedido.class));
        verify(servicioEntregaDAO, times(1)).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(repartidorDAO, times(1)).findByCodigoPostalOrderByEficienciaAsc(ArgumentMatchers.any(CodigoPostal.class));
        verify(repartidorDAO, times(1)).save(ArgumentMatchers.any(Repartidor.class));

        assertThat(session.getAttribute("carrito"), is(nullValue()));
        assertThat(session.getAttribute("pedido"), is(nullValue()));
        assertThat(session.getAttribute("direccionId"), is(nullValue()));
        assertThat(session.getAttribute("metodoPago"), is(nullValue()));
        assertThat(session.getAttribute("restauranteId"), is(nullValue()));
    }

    @Test
    public void testFinalizarPedido_ErrorProcesamiento() throws Exception {
        Cliente cliente = crearCliente();
        Restaurante restaurante = crearRestaurante(12300); // Usar código postal válido
        Pedido pedido = crearPedido(restaurante);
        Direccion direccion = crearDireccion(12300); // Usar código postal válido
        Map<Long, Integer> carrito = new HashMap<>();
        carrito.put(1L, 2);
        ItemMenu item = crearItemMenu(1L, 10.0);
        session.setAttribute("cliente", cliente);
        session.setAttribute("carrito", carrito);

        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));
        when(direccionDAO.findById(1L)).thenReturn(Optional.of(direccion));
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));
        when(pagoDAO.save(ArgumentMatchers.any(Pago.class))).thenThrow(new RuntimeException("Error de base de datos"));

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