package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.CartaMenuDAO;
import proyecto.iso2.persistencia.RestauranteDAO;
import proyecto.iso2.persistencia.UsuarioDAO;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CartaMenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    private MockHttpSession session;
    private Restaurante restaurante;
    private CartaMenu carta;

    @BeforeEach
    public void setUp() {
        cartaMenuDAO.deleteAll();
        restauranteDAO.deleteAll();
        usuarioDAO.deleteAll();

        restaurante = new Restaurante(
                "restaurante1@ejemplo.com",
                "pass123",
                "Restaurante A",
                "CIF1",
                null
        );
        restaurante = restauranteDAO.save(restaurante);

        carta = new CartaMenu();
        carta.setNombre("Menú Principal");
        carta.setRestaurante(restaurante);
        carta = cartaMenuDAO.save(carta);

        session = new MockHttpSession();
        session.setAttribute("restaurante", restaurante);
    }

    @Test
    public void testMostrarFormularioCreacion() throws Exception {
        mockMvc.perform(get("/cartas/crear")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("crearCarta"))
                .andExpect(model().attributeExists("carta"));
    }

    @Test
    public void testCrearCarta() throws Exception {
        mockMvc.perform(post("/cartas/crear")
                        .session(session)
                        .param("nombre", "Menú Nuevo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));

        assertThat(cartaMenuDAO.findByRestaurante(restaurante), hasSize(2));
        assertThat(cartaMenuDAO.findByRestaurante(restaurante), hasItem(
                hasProperty("nombre", is("Menú Nuevo"))
        ));
    }

    @Test
    public void testCrearCarta_SinSesion() throws Exception {
        mockMvc.perform(post("/cartas/crear")
                        .param("nombre", "Menú Nuevo")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMostrarFormularioEdicion() throws Exception {
        mockMvc.perform(get("/cartas/editar/" + carta.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("editarCarta"))
                .andExpect(model().attributeExists("carta"))
                .andExpect(model().attribute("carta", hasProperty("nombre", is("Menú Principal"))))
                .andExpect(model().attributeExists("menus"))
                .andExpect(model().attributeExists("nuevoMenu"));
    }
}