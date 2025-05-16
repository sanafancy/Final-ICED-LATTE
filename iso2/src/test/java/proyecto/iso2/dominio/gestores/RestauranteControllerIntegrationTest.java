package proyecto.iso2.dominio.gestores;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RestauranteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private MockHttpSession sessionCliente;
    private MockHttpSession sessionRestaurante;
    private Cliente cliente;
    private Restaurante restaurante;


    @BeforeEach
    public void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        // Inicializamos el TransactionTemplate con el transactionManager
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Limpiamos la base de datos respetando las dependencias
        transactionTemplate.execute(status -> {
            // Eliminar servicio_entrega primero para evitar violación con pedido
            entityManager.createNativeQuery("DELETE FROM servicio_entrega").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            // Desvincular PAGO_ID en pedido para evitar violación de clave foránea
            entityManager.createNativeQuery("UPDATE pedido SET pago_id = NULL").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM pago").executeUpdate();
            entityManager.createQuery("DELETE FROM Pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });

        // Crear un cliente para las pruebas
        cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        cliente = usuarioDAO.save(cliente);

        // Crear un restaurante para las pruebas
        restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante = restauranteDAO.save(restaurante);

        // Configurar sesiones
        sessionCliente = new MockHttpSession();
        sessionCliente.setAttribute("cliente", cliente);

        sessionRestaurante = new MockHttpSession();
        sessionRestaurante.setAttribute("restaurante", restaurante);
        sessionRestaurante.setAttribute("restauranteId", restaurante.getIdUsuario());
    }


    @Test
    public void testBuscarRestaurante_PorCalle() throws Exception {
        // Crear una dirección y asociarla al restaurante
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        direccion = direccionDAO.save(direccion);

        restaurante.setDireccion(direccion);
        restauranteDAO.save(restaurante);

        mockMvc.perform(get("/buscarRestaurante")
                        .param("busqueda", "Calle Principal")
                        .session(sessionCliente))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("restaurantes"))
                .andExpect(model().attribute("restaurantes", hasSize(1)))
                .andExpect(model().attribute("restaurantes", contains(hasProperty("nombre", is("Restaurante A")))));
    }


    @Test
    public void testInicioRestaurante_ConRestaurante() throws Exception {
        // Crear una carta de menú para el restaurante
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Principal");
        carta.setRestaurante(restaurante);
        carta = cartaMenuDAO.save(carta);

        mockMvc.perform(get("/inicioRestaurante")
                        .session(sessionRestaurante))
                .andExpect(status().isOk())
                .andExpect(view().name("inicioRestaurante"))
                .andExpect(model().attributeExists("restaurante"))
                .andExpect(model().attributeExists("cartas"))
                .andExpect(model().attribute("cartas", hasSize(1)))
                .andExpect(model().attribute("cartas", contains(hasProperty("nombre", is("Carta Principal")))));
    }


    @Test
    public void testInicioRestaurante_SinRestaurante() throws Exception {
        MockHttpSession sessionSinRestaurante = new MockHttpSession();
        mockMvc.perform(get("/inicioRestaurante")
                        .session(sessionSinRestaurante))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }


    @Test
    public void testToggleFavorito_AgregarFavorito() throws Exception {
        mockMvc.perform(post("/favorito/" + restaurante.getIdUsuario())
                        .session(sessionCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el restaurante se añadió a los favoritos del cliente
        Cliente clienteActualizado = clienteDAO.findById(cliente.getIdUsuario()).orElse(null);
        assertThat(clienteActualizado.getFavoritos(), hasSize(1));
        assertThat(clienteActualizado.getFavoritos(), contains(hasProperty("nombre", is("Restaurante A"))));
    }


    @Test
    public void testVerFavoritos_ConCliente() throws Exception {
        // Crear una dirección para el restaurante
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Restaurante");
        direccion.setNumero(123);
        direccion.setComplemento("Local 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        direccion = direccionDAO.save(direccion);

        // Asignar la dirección al restaurante y guardar
        restaurante.setDireccion(direccion);
        restauranteDAO.save(restaurante);

        // Añadir el restaurante a los favoritos del cliente
        cliente.getFavoritos().add(restaurante);
        clienteDAO.save(cliente);

        mockMvc.perform(get("/cliente/favoritos")
                        .session(sessionCliente))
                .andExpect(status().isOk())
                .andExpect(view().name("favoritos"))
                .andExpect(model().attributeExists("favoritos"))
                .andExpect(model().attribute("favoritos", hasSize(1)))
                .andExpect(model().attribute("favoritos", contains(hasProperty("nombre", is("Restaurante A")))));
    }


    @Test
    public void testVerFavoritos_SinCliente() throws Exception {
        MockHttpSession sessionSinCliente = new MockHttpSession();
        mockMvc.perform(get("/cliente/favoritos")
                        .session(sessionSinCliente))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }


    @Test
    public void testEliminarRestaurante() throws Exception {
        // Crear una carta de menú para el restaurante
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Principal");
        carta.setRestaurante(restaurante);
        carta = cartaMenuDAO.save(carta);

        // Crear un ítem para la carta
        ItemMenu item = new ItemMenu();
        item.setNombre("Pizza");
        item.setPrecio(10.0);
        item.setTipo("Plato");
        item.setCartaMenu(carta);
        itemMenuDAO.save(item);

        mockMvc.perform(post("/eliminarRestaurante")
                        .session(sessionRestaurante))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el restaurante, cartas e ítems fueron eliminados
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        List<CartaMenu> cartas = cartaMenuDAO.findAll();
        List<ItemMenu> items = itemMenuDAO.findAll();
        assertThat(restaurantes, hasSize(0));
        assertThat(cartas, hasSize(0));
        assertThat(items, hasSize(0));
    }


    @Test
    public void testVerMenuRestaurante_RestauranteExistente() throws Exception {
        // Crear una carta de menú con ítems
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Principal");
        carta.setRestaurante(restaurante);
        carta = cartaMenuDAO.save(carta);

        ItemMenu item = new ItemMenu();
        item.setNombre("Pizza");
        item.setPrecio(10.0);
        item.setTipo("Plato");
        item.setCartaMenu(carta);
        itemMenuDAO.save(item);

        // Verificar que la carta se guardó correctamente
        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
        assertThat(cartas, hasSize(1));
        assertThat(cartas, contains(hasProperty("nombre", is("Carta Principal"))));

        // Crear una dirección para el cliente
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Cliente");
        direccion.setNumero(456);
        direccion.setComplemento("Piso 2");
        direccion.setMunicipio("Ciudad Cliente");
        direccion.setCodigoPostal(54321);
        direccion = direccionDAO.save(direccion);

        cliente.getDirecciones().add(direccion);
        clienteDAO.save(cliente);

        mockMvc.perform(get("/restaurante/" + restaurante.getIdUsuario())
                        .session(sessionCliente))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attributeExists("restaurante"))
                .andExpect(model().attribute("restaurante", hasProperty("nombre", is("Restaurante A"))))
                .andExpect(model().attributeExists("cartas"))
                .andExpect(model().attribute("cartas", hasSize(1)))
                .andExpect(model().attribute("cartas", contains(hasProperty("nombre", is("Carta Principal")))))
                .andExpect(model().attributeExists("direcciones"))
                .andExpect(model().attribute("direcciones", hasSize(1)))
                .andExpect(model().attribute("direcciones", contains(hasProperty("calle", is("Calle Cliente")))));
    }


    @Test
    public void testVerMenuRestaurante_RestauranteNoExistente() throws Exception {
        mockMvc.perform(get("/restaurante/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}