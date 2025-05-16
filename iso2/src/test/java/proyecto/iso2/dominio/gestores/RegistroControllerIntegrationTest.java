package proyecto.iso2.dominio.gestores;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.ClienteDAO;
import proyecto.iso2.persistencia.RepartidorDAO;
import proyecto.iso2.persistencia.RestauranteDAO;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegistroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private RepartidorDAO repartidorDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() {
        // Configuración manual de MockMvc como respaldo
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        // Inicializamos el TransactionTemplate con el transactionManager
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Limpiamos la base de datos respetando las dependencias
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM Pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });
    }

    @Test
    public void testShowRegistroPage() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"));
    }

    @Test
    public void testShowRegistroClientePage() throws Exception {
        mockMvc.perform(get("/registro/cliente"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroCliente"));
    }

    @Test
    public void testRegistrarCliente() throws Exception {
        mockMvc.perform(post("/registro/cliente")
                        .param("email", "cliente@ejemplo.com")
                        .param("pass", "pass123")
                        .param("nombre", "Cliente A")
                        .param("apellidos", "Apellido A")
                        .param("dni", "12345678A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el cliente se ha registrado correctamente
        List<Cliente> clientes = clienteDAO.findAll();
        assertThat(clientes, hasSize(1));
        assertThat(clientes.get(0).getEmail(), is("cliente@ejemplo.com"));
        assertThat(clientes.get(0).getNombre(), is("Cliente A"));
        assertThat(clientes.get(0).getApellidos(), is("Apellido A"));
        assertThat(clientes.get(0).getDni(), is("12345678A"));
    }

    @Test
    public void testShowRegistroRestaurantePage() throws Exception {
        mockMvc.perform(get("/registro/restaurante"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroRestaurante"));
    }

    @Test
    public void testRegistrarRestaurante() throws Exception {
        mockMvc.perform(post("/registro/restaurante")
                        .param("email", "restaurante@ejemplo.com")
                        .param("pass", "pass123")
                        .param("nombre", "Restaurante A")
                        .param("cif", "CIF123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el restaurante se ha registrado correctamente
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getEmail(), is("restaurante@ejemplo.com"));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante A"));
        assertThat(restaurantes.get(0).getCif(), is("CIF123"));
    }

    @Test
    public void testShowRegistroRepartidorPage() throws Exception {
        mockMvc.perform(get("/registro/repartidor"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroRepartidor"));
    }

    @Test
    public void testRegistrarRepartidor() throws Exception {
        mockMvc.perform(post("/registro/repartidor")
                        .param("email", "repartidor@ejemplo.com")
                        .param("pass", "pass123")
                        .param("nombre", "Repartidor A")
                        .param("apellidos", "Apellido A")
                        .param("nif", "NIF123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el repartidor se ha registrado correctamente
        List<Repartidor> repartidores = repartidorDAO.findAll();
        assertThat(repartidores, hasSize(1));
        assertThat(repartidores.get(0).getEmail(), is("repartidor@ejemplo.com"));
        assertThat(repartidores.get(0).getNombre(), is("Repartidor A"));
        assertThat(repartidores.get(0).getApellidos(), is("Apellido A"));
        assertThat(repartidores.get(0).getNif(), is("NIF123"));
    }
}