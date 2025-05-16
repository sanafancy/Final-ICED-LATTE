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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RepartidorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepartidorDAO repartidorDAO;

    @Autowired
    private ServicioEntregaDAO servicioEntregaDAO;

    @Autowired
    private PedidoDAO pedidoDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private PagoDAO pagoDAO;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        session = new MockHttpSession();

        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("DELETE FROM servicio_entrega").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            entityManager.createNativeQuery("UPDATE pedido SET pago_id = NULL").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pago").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });
    }

    private Repartidor createRepartidor() {
        return transactionTemplate.execute(status -> {
            Repartidor r = new Repartidor();
            r.setNombre("Repartidor A");
            r.setNif("12345678Z");
            r.setEficiencia(5);
            return repartidorDAO.save(r);
        });
    }

    private Object[] createPedidoSetup(Repartidor repartidor) {
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
            Direccion d = new Direccion();
            d.setCodigoPostal(12300);
            d.setCalle("Calle Restaurante");
            d.setNumero(10);
            d.setMunicipio("Madrid");
            d = direccionDAO.save(d);
            Restaurante r = new Restaurante();
            r.setNombre("Restaurante A");
            r.setCif("B12345678");
            r.setDireccion(d);
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

        Pago pago = transactionTemplate.execute(status -> {
            Pago p = new Pago();
            p.setMetodoPago(MetodoPago.CREDIT_CARD);
            p.setFechaTransaccion(LocalDateTime.now());
            return pagoDAO.save(p);
        });

        Pedido pedido = transactionTemplate.execute(status -> {
            Pedido p = new Pedido();
            p.setCliente(cliente);
            p.setRestaurante(restaurante);
            p.setEstado(EstadoPedido.PAGADO);
            p.setFecha(LocalDateTime.now());
            p.setTotal(20.0);
            p.setMetodoPago(MetodoPago.CREDIT_CARD);
            p.setPago(pago);
            return pedidoDAO.save(p);
        });

        pago.setPedido(pedido);
        transactionTemplate.execute(status -> pagoDAO.save(pago));

        ServicioEntrega servicio = transactionTemplate.execute(status -> {
            ServicioEntrega s = new ServicioEntrega();
            s.setRepartidor(repartidor);
            s.setPedido(pedido);
            s.setDireccion(direccion);
            return servicioEntregaDAO.save(s);
        });

        return new Object[]{pedido, servicio, direccion};
    }

    @Test
    public void testMostrarFormularioCodigoPostal() throws Exception {
        Repartidor repartidor = createRepartidor();
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(get("/repartidor/seleccionar-codigo-postal").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("seleccionarCodigoPostal"))
                .andExpect(model().attribute("codigosPostales", is(CodigoPostal.values())));
    }

    @Test
    public void testMostrarFormularioSinRepartidor() throws Exception {
        mockMvc.perform(get("/repartidor/seleccionar-codigo-postal").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testGuardarCodigoPostal() throws Exception {
        Repartidor repartidor = createRepartidor();
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/seleccionar-codigo-postal")
                        .param("codigoPostal", "0")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        Repartidor updated = repartidorDAO.findById(repartidor.getIdUsuario()).orElseThrow();
        assertThat(updated.getCodigoPostal(), is(CodigoPostal.CP_12300));
    }

    @Test
    public void testMostrarInicioRepartidor() throws Exception {
        Repartidor repartidor = createRepartidor();
        Object[] setup = createPedidoSetup(repartidor);
        ServicioEntrega servicio = (ServicioEntrega) setup[1];
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(get("/repartidor/InicioRepartidor").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("InicioRepartidor"))
                .andExpect(model().attributeExists("serviciosEntrega"))
                .andExpect(model().attribute("serviciosEntrega", hasSize(1)))
                .andExpect(model().attribute("serviciosEntrega", contains(hasProperty("id", is(servicio.getId())))));
    }

    @Test
    public void testMarcarRecogido() throws Exception {
        Repartidor repartidor = createRepartidor();
        Object[] setup = createPedidoSetup(repartidor);
        Pedido pedido = (Pedido) setup[0];
        ServicioEntrega servicio = (ServicioEntrega) setup[1];
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", servicio.getId().toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        ServicioEntrega updatedServicio = servicioEntregaDAO.findById(servicio.getId()).orElseThrow();
        assertThat(updatedServicio.getFechaRecepcion(), is(notNullValue()));
        Pedido updatedPedido = pedidoDAO.findById(pedido.getId()).orElseThrow();
        assertThat(updatedPedido.getEstado(), is(EstadoPedido.RECOGIDO));
    }

    @Test
    public void testMarcarRecogidoNoAutorizado() throws Exception {
        Repartidor repartidor1 = createRepartidor();
        Repartidor repartidor2 = transactionTemplate.execute(status -> {
            Repartidor r = new Repartidor();
            r.setNombre("Repartidor B");
            r.setNif("87654321X");
            r.setEficiencia(4);
            return repartidorDAO.save(r);
        });
        Object[] setup = createPedidoSetup(repartidor1);
        Pedido pedido = (Pedido) setup[0];
        ServicioEntrega servicio = (ServicioEntrega) setup[1];
        session.setAttribute("repartidor", repartidor2);

        mockMvc.perform(post("/repartidor/marcar-recogido")
                        .param("servicioId", servicio.getId().toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        ServicioEntrega updatedServicio = servicioEntregaDAO.findById(servicio.getId()).orElseThrow();
        assertThat(updatedServicio.getFechaRecepcion(), is(nullValue()));
        Pedido updatedPedido = pedidoDAO.findById(pedido.getId()).orElseThrow();
        assertThat(updatedPedido.getEstado(), is(EstadoPedido.PAGADO));
    }

    @Test
    public void testMarcarEntregado() throws Exception {
        Repartidor repartidor = createRepartidor();
        Object[] setup = createPedidoSetup(repartidor);
        Pedido pedido = (Pedido) setup[0];
        ServicioEntrega servicio = (ServicioEntrega) setup[1];
        transactionTemplate.execute(status -> {
            servicio.setFechaRecepcion(LocalDateTime.now());
            pedido.setEstado(EstadoPedido.RECOGIDO);
            servicioEntregaDAO.save(servicio);
            pedidoDAO.save(pedido);
            return null;
        });
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", servicio.getId().toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        ServicioEntrega updatedServicio = servicioEntregaDAO.findById(servicio.getId()).orElseThrow();
        assertThat(updatedServicio.getFechaEntrega(), is(notNullValue()));
        Pedido updatedPedido = pedidoDAO.findById(pedido.getId()).orElseThrow();
        assertThat(updatedPedido.getEstado(), is(EstadoPedido.ENTREGADO));
    }

    @Test
    public void testMarcarEntregadoSinFechaRecepcion() throws Exception {
        Repartidor repartidor = createRepartidor();
        Object[] setup = createPedidoSetup(repartidor);
        Pedido pedido = (Pedido) setup[0];
        ServicioEntrega servicio = (ServicioEntrega) setup[1];
        session.setAttribute("repartidor", repartidor);

        mockMvc.perform(post("/repartidor/marcar-entregado")
                        .param("servicioId", servicio.getId().toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/repartidor/InicioRepartidor"));

        ServicioEntrega updatedServicio = servicioEntregaDAO.findById(servicio.getId()).orElseThrow();
        assertThat(updatedServicio.getFechaEntrega(), is(nullValue()));
        Pedido updatedPedido = pedidoDAO.findById(pedido.getId()).orElseThrow();
        assertThat(updatedPedido.getEstado(), is(EstadoPedido.PAGADO));
    }
}