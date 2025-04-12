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
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.UsuarioDAO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioDAO usuarioDAO;

    private MockHttpSession session;
    private Cliente cliente;
    private Restaurante restaurante;
    private Repartidor repartidor;

    @BeforeEach
    public void setUp() {
        usuarioDAO.deleteAll();

        // Crear un Cliente
        cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        usuarioDAO.save(cliente);

        // Crear un Restaurante
        restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        usuarioDAO.save(restaurante);

        // Crear un Repartidor
        repartidor = new Repartidor();
        repartidor.setEmail("repartidor@ejemplo.com");
        repartidor.setPass("pass123");
        repartidor.setNombre("Repartidor A");
        repartidor.setApellidos("Apellido B");
        repartidor.setNif("NIF123");
        usuarioDAO.save(repartidor);

        session = new MockHttpSession();
    }

    @Test
    public void testShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void testLoginRestauranteExitoso() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "restaurante@ejemplo.com")
                        .param("pass", "pass123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));

        // Verificar que el restaurante está en la sesión
        Restaurante restauranteEnSesion = (Restaurante) session.getAttribute("restaurante");
        assert restauranteEnSesion != null;
        assert restauranteEnSesion.getEmail().equals("restaurante@ejemplo.com");
    }

    @Test
    public void testLoginClienteExitoso() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "cliente@ejemplo.com")
                        .param("pass", "pass123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el cliente está en la sesión
        Cliente clienteEnSesion = (Cliente) session.getAttribute("cliente");
        assert clienteEnSesion != null;
        assert clienteEnSesion.getEmail().equals("cliente@ejemplo.com");
    }

    @Test
    public void testLoginRepartidorExitoso() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "repartidor@ejemplo.com")
                        .param("pass", "pass123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("InicioRepartidor"));

        // Verificar que el repartidor está en la sesión
        Repartidor repartidorEnSesion = (Repartidor) session.getAttribute("repartidor");
        assert repartidorEnSesion != null;
        assert repartidorEnSesion.getEmail().equals("repartidor@ejemplo.com");
    }

    @Test
    public void testLoginFallido() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "usuario@inexistente.com")
                        .param("pass", "pass123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // Verificar que no hay usuario en la sesión
        assert session.getAttribute("usuario") == null;
        assert session.getAttribute("cliente") == null;
        assert session.getAttribute("restaurante") == null;
        assert session.getAttribute("repartidor") == null;
    }

    @Test
    public void testLogout() throws Exception {
        // Iniciar sesión para un restaurante
        session.setAttribute("restaurante", restaurante);
        session.setAttribute("usuario", restaurante);

        // Verificar que los atributos están en la sesión antes del logout
        assert session.getAttribute("restaurante") != null;
        assert session.getAttribute("usuario") != null;

        // Llamar a logout y verificar la redirección
        mockMvc.perform(get("/logout")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}