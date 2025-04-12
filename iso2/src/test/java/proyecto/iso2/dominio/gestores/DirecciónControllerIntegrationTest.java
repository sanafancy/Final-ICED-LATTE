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
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.DireccionDAO;
import proyecto.iso2.persistencia.RestauranteDAO;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DirecciónControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private MockHttpSession session;
    private Restaurante restaurante;
    private Direccion direccion;

    @BeforeEach
    public void setUp() {

        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        // Inicializamos el TransactionTemplate con el transactionManager
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Ejecutamos las operaciones de eliminación dentro de una transacción, respetando las dependencias
        transactionTemplate.execute(status -> {
            // Eliminar registros de las tablas que dependen de direccion
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            // Eliminar registros de direccion
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            // Eliminar registros de usuario
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            return null;
        });

        // Crear un restaurante
        restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante = restauranteDAO.save(restaurante);

        // Configurar la sesión
        session = new MockHttpSession();
        session.setAttribute("restaurante", restaurante);
    }

    @Test
    public void testMostrarDireccion_ConRestauranteYDireccion() throws Exception {
        // Crear una dirección y asociarla al restaurante
        direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);
        direccion = direccionDAO.save(direccion);

        restaurante.setDireccion(direccion);
        restauranteDAO.save(restaurante);

        mockMvc.perform(get("/direcciones")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("direcciones"))
                .andExpect(model().attributeExists("direccion"))
                .andExpect(model().attribute("direccion", hasProperty("calle", is("Calle Principal"))))
                .andExpect(model().attribute("direccion", hasProperty("numero", is(123))))
                .andExpect(model().attribute("direccion", hasProperty("complemento", is("Piso 1"))))
                .andExpect(model().attribute("direccion", hasProperty("municipio", is("Ciudad"))))
                .andExpect(model().attribute("direccion", hasProperty("codigoPostal", is(12345))));
    }

    @Test
    public void testMostrarDireccion_ConRestauranteSinDireccion() throws Exception {
        // El restaurante no tiene dirección asociada
        mockMvc.perform(get("/direcciones")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("direcciones"))
                .andExpect(model().attributeExists("direccion"))
                .andExpect(model().attribute("direccion", hasProperty("calle", is(nullValue()))))
                .andExpect(model().attribute("direccion", hasProperty("numero", is(0))))
                .andExpect(model().attribute("direccion", hasProperty("complemento", is(nullValue()))))
                .andExpect(model().attribute("direccion", hasProperty("municipio", is(nullValue()))))
                .andExpect(model().attribute("direccion", hasProperty("codigoPostal", is(0))));
    }

    @Test
    public void testMostrarDireccion_SinRestaurante() throws Exception {
        MockHttpSession sessionSinRestaurante = new MockHttpSession();
        mockMvc.perform(get("/direcciones")
                        .session(sessionSinRestaurante))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testEditarDireccion_ConRestauranteYDireccion() throws Exception {
        // Crear una dirección y asociarla al restaurante
        direccion = new Direccion();
        direccion.setCalle("Calle Vieja");
        direccion.setNumero(456);
        direccion.setComplemento("Piso 2");
        direccion.setMunicipio("Pueblo");
        direccion.setCodigoPostal(54321);
        direccion = direccionDAO.save(direccion);

        restaurante.setDireccion(direccion);
        restauranteDAO.save(restaurante);

        // Enviar una solicitud para editar la dirección
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Nueva")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("municipio", "Ciudad Nueva")
                        .param("codigoPostal", "98765")
                        .param("id", direccion.getId().toString())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/direcciones"));

        // Verificar que la dirección se ha actualizado buscando todas las direcciones
        List<Direccion> direcciones = direccionDAO.findAll();
        assertThat(direcciones, hasSize(1));
        Direccion direccionActualizada = direcciones.get(0);
        assertThat(direccionActualizada.getCalle(), is("Calle Nueva"));
        assertThat(direccionActualizada.getNumero(), is(789));
        assertThat(direccionActualizada.getComplemento(), is("Piso 3"));
        assertThat(direccionActualizada.getMunicipio(), is("Ciudad Nueva"));
        assertThat(direccionActualizada.getCodigoPostal(), is(98765));
    }

    @Test
    public void testEditarDireccion_ConRestauranteSinDireccion() throws Exception {
        // El restaurante no tiene dirección asociada
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Nueva")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("municipio", "Ciudad Nueva")
                        .param("codigoPostal", "98765")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/direcciones"));

        // Verificar que no se creó una nueva dirección (ya que el controlador no la crea)
        assertThat(restaurante.getDireccion(), is(nullValue()));
        List<Direccion> direcciones = direccionDAO.findAll();
        assertThat(direcciones, hasSize(0));
    }

    @Test
    public void testEditarDireccion_SinRestaurante() throws Exception {
        MockHttpSession sessionSinRestaurante = new MockHttpSession();
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Nueva")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("municipio", "Ciudad Nueva")
                        .param("codigoPostal", "98765")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(sessionSinRestaurante))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}