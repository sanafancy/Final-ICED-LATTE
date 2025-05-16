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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RepartidorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RepartidorDAO repartidorDAO;

    @Mock
    private ServicioEntregaDAO servicioEntregaDAO;

    @Mock
    private PedidoDAO pedidoDAO;

    @InjectMocks
    private RepartidorController repartidorController;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(repartidorDAO, servicioEntregaDAO, pedidoDAO);

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

        mockMvc = MockMvcBuilders.standaloneSetup(repartidorController)
                .setViewResolvers(viewResolver)
                .build();

        session = new MockHttpSession();
    }

    private Repartidor crearRepartidor() {
        Repartidor repartidor = new Repartidor("repartidor@email.com", "pass123", "Ana", "García", "87654321B", 90);
        setIdUsuario(repartidor, 1L);
        return repartidor;
    }

    private Restaurante crearRestaurante() {
        Direccion direccion = crearDireccion();
        Restaurante restaurante = new Restaurante("restaurante@email.com", "pass123", "Restaurante A", "CIF123", direccion);
        setIdUsuario(restaurante, 3L);
        return restaurante;
    }

    private Direccion crearDireccion() {
        Direccion direccion = new Direccion("Calle Restaurante", 123, "Local 1", 12300, "Madrid");
        trySetId(direccion, 2L);
        return direccion;
    }

    private Pedido crearPedido() {
        Pedido pedido = new Pedido();
        trySetId(pedido, 1L);
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setRestaurante(crearRestaurante());
        return pedido;
    }

    private ServicioEntrega crearServicioEntrega(Repartidor repartidor, Pedido pedido) {
        ServicioEntrega servicio = new ServicioEntrega();
        trySetId(servicio, 1L);
        servicio.setRepartidor(repartidor);
        servicio.setPedido(pedido);
        servicio.setDireccion(crearDireccion()); // Establecer la dirección para evitar null en la plantilla
        return servicio;
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
        String[] possibleSetters = {"setId", "setIdServicio", "setIdPedido", "setIdDireccion"};
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
    public void testMostrarFormularioCodigoPostal_SinRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/seleccionar-codigo-postal")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMostrarFormularioCodigoPostal_ConRepartidor() throws Exception {
        Repartidor repartidor = crearRepartidor();
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(get("/repartidor/seleccionar-codigo-postal")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("seleccionarCodigoPostal"))
                .andExpect(model().attribute("repartidor", is(repartidor)))
                .andExpect(model().attribute("codigosPostales", is(CodigoPostal.values())));
    }

    @Test
    public void testGuardarCodigoPostal_SinRepartidor() throws Exception {
        mockMvc.perform(post("/repartidor/seleccionar-codigo-postal")
                        .param("codigoPostal", "0")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testGuardarCodigoPostal_CodigoInvalido() throws Exception {
        Repartidor repartidor = crearRepartidor();
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/seleccionar-codigo-postal")
                        .param("codigoPostal", "999")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/seleccionar-codigo-postal?error=invalid"));

        verify(repartidorDAO, never()).save(ArgumentMatchers.any(Repartidor.class));
    }

    @Test
    public void testGuardarCodigoPostal_Exitoso() throws Exception {
        Repartidor repartidor = crearRepartidor();
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/seleccionar-codigo-postal")
                        .param("codigoPostal", "0") // Índice 0 corresponde a CP_12300
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(repartidorDAO, times(1)).save(repartidor);
        assertThat(repartidor.getCodigoPostal(), is(CodigoPostal.CP_12300));
    }

    @Test
    public void testMostrarInicioRepartidor_SinRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/InicioRepartidor")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMostrarInicioRepartidor_ConRepartidor() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(repartidor, pedido);
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findByRepartidor_IdUsuario(repartidor.getIdUsuario()))
                .thenReturn(Arrays.asList(servicio));

        mockMvc.perform(get("/repartidor/InicioRepartidor")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("InicioRepartidor"))
                .andExpect(model().attribute("repartidor", is(repartidor)))
                .andExpect(model().attribute("serviciosEntrega", hasItem(servicio)));
    }

    @Test
    public void testMarcarRecogido_SinRepartidor() throws Exception {
        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMarcarRecogido_ServicioInexistente() throws Exception {
        Repartidor repartidor = crearRepartidor();
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, never()).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(pedidoDAO, never()).save(ArgumentMatchers.any(Pedido.class));
    }

    @Test
    public void testMarcarRecogido_ServicioNoAutorizado() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Repartidor otroRepartidor = crearRepartidor();
        setIdUsuario(otroRepartidor, 2L);
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(otroRepartidor, pedido);
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.of(servicio));

        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, never()).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(pedidoDAO, never()).save(ArgumentMatchers.any(Pedido.class));
    }

    @Test
    public void testMarcarRecogido_Exitoso() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(repartidor, pedido);
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioEntregaDAO.save(ArgumentMatchers.any(ServicioEntrega.class))).thenReturn(servicio);
        when(pedidoDAO.save(ArgumentMatchers.any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, times(1)).save(servicio);
        verify(pedidoDAO, times(1)).save(pedido);
        assertThat(servicio.getFechaRecepcion(), is(notNullValue()));
        assertThat(pedido.getEstado(), is(EstadoPedido.RECOGIDO));
    }

    @Test
    public void testMarcarEntregado_SinRepartidor() throws Exception {
        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMarcarEntregado_ServicioInexistente() throws Exception {
        Repartidor repartidor = crearRepartidor();
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, never()).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(pedidoDAO, never()).save(ArgumentMatchers.any(Pedido.class));
    }

    @Test
    public void testMarcarEntregado_ServicioNoAutorizado() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Repartidor otroRepartidor = crearRepartidor();
        setIdUsuario(otroRepartidor, 2L);
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(otroRepartidor, pedido);
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.of(servicio));

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, never()).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(pedidoDAO, never()).save(ArgumentMatchers.any(Pedido.class));
    }

    @Test
    public void testMarcarEntregado_SinFechaRecepcion() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(repartidor, pedido);
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.of(servicio));

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, never()).save(ArgumentMatchers.any(ServicioEntrega.class));
        verify(pedidoDAO, never()).save(ArgumentMatchers.any(Pedido.class));
    }

    @Test
    public void testMarcarEntregado_Exitoso() throws Exception {
        Repartidor repartidor = crearRepartidor();
        Pedido pedido = crearPedido();
        ServicioEntrega servicio = crearServicioEntrega(repartidor, pedido);
        servicio.setFechaRecepcion(LocalDateTime.now());
        session.setAttribute("repartidor", repartidor);

        when(servicioEntregaDAO.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioEntregaDAO.save(ArgumentMatchers.any(ServicioEntrega.class))).thenReturn(servicio);
        when(pedidoDAO.save(ArgumentMatchers.any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", "1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        verify(servicioEntregaDAO, times(1)).save(servicio);
        verify(pedidoDAO, times(1)).save(pedido);
        assertThat(servicio.getFechaEntrega(), is(notNullValue()));
        assertThat(pedido.getEstado(), is(EstadoPedido.ENTREGADO));
    }
}