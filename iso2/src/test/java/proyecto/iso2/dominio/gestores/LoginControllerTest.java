package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.UsuarioDAO;
import org.springframework.mock.web.MockHttpSession;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LoginControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioDAO usuarioDAO;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        usuarioDAO = mock(UsuarioDAO.class);

        // Crear una instancia del controlador
        LoginController controller = new LoginController();

        // Usar reflexión para inyectar el mock en el campo privado
        injectMock(controller, "usuarioDAO", usuarioDAO);

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
    }

    // Método auxiliar para inyectar un mock en un campo privado usando reflexión
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // Método auxiliar para crear un cliente con datos válidos
    private Cliente crearCliente(String nombre, String apellidos, String email, String pass, String dni) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        cliente.setDirecciones(new ArrayList<>()); // Inicializar direcciones para evitar NullPointerException
        cliente.setFavoritos(new ArrayList<>()); // Inicializar favoritos para evitar NullPointerException
        return cliente;
    }

    // Método auxiliar para crear un restaurante con datos válidos
    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, String calle, String municipio, int codigoPostal) {
        Direccion direccion = new Direccion(calle, 1, "", codigoPostal, municipio); // Número por defecto: 1, complemento vacío
        return new Restaurante(email, pass, nombre, cif, direccion);
    }

    // Método auxiliar para crear un repartidor con datos válidos
    private Repartidor crearRepartidor(String nombre, String apellidos, String email, String pass, String nif) {
        return new Repartidor(email, pass, nombre, apellidos, nif, 5); // Eficiencia por defecto: 5
    }

    // Pruebas para el método showLoginPage (GET /login)
    @Test
    public void testShowLoginPage() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    // Pruebas para el método login (POST /login)
    @Test
    public void testLogin_ClienteExitoso() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        when(usuarioDAO.findByEmailAndPass("cliente@ejemplo.com", "pass123")).thenReturn(Optional.of(cliente));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/login")
                        .param("email", "cliente@ejemplo.com")
                        .param("pass", "pass123")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que el cliente se guardó en la sesión
        assertEquals(cliente, session.getAttribute("usuario"));
        assertEquals(cliente, session.getAttribute("cliente"));
    }

    @Test
    public void testLogin_RestauranteExitoso() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", "Calle 123", "Madrid", 28001);
        when(usuarioDAO.findByEmailAndPass("restaurante@ejemplo.com", "pass123")).thenReturn(Optional.of(restaurante));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/login")
                        .param("email", "restaurante@ejemplo.com")
                        .param("pass", "pass123")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));

        // Verificar que el restaurante se guardó en la sesión
        assertEquals(restaurante, session.getAttribute("usuario"));
        assertEquals(restaurante, session.getAttribute("restaurante"));
    }

    @Test
    public void testLogin_RepartidorExitoso() throws Exception {
        // Datos de prueba
        Repartidor repartidor = crearRepartidor("Repartidor", "Uno", "repartidor@ejemplo.com", "pass123", "12345678B");
        when(usuarioDAO.findByEmailAndPass("repartidor@ejemplo.com", "pass123")).thenReturn(Optional.of(repartidor));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/login")
                        .param("email", "repartidor@ejemplo.com")
                        .param("pass", "pass123")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("InicioRepartidor"));

        // Verificar que el repartidor se guardó en la sesión
        assertEquals(repartidor, session.getAttribute("usuario"));
        assertEquals(repartidor, session.getAttribute("repartidor"));
    }

    @Test
    public void testLogin_Fallido() throws Exception {
        // Configurar el mock para que no encuentre el usuario
        when(usuarioDAO.findByEmailAndPass(anyString(), anyString())).thenReturn(Optional.empty());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/login")
                        .param("email", "noexiste@ejemplo.com")
                        .param("pass", "pass123")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // Verificar que no se guardó nada en la sesión
        assertNull(session.getAttribute("usuario"));
        assertNull(session.getAttribute("cliente"));
        assertNull(session.getAttribute("restaurante"));
        assertNull(session.getAttribute("repartidor"));
    }

    @Test
    public void testLogin_ParametrosInvalidos() throws Exception {
        // Ejecutar la solicitud POST con parámetros vacíos
        mockMvc.perform(post("/login")
                        .param("email", "")
                        .param("pass", "")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // Verificar que no se guardó nada en la sesión
        assertNull(session.getAttribute("usuario"));
        assertNull(session.getAttribute("cliente"));
        assertNull(session.getAttribute("restaurante"));
        assertNull(session.getAttribute("repartidor"));
    }

    // Pruebas para el método logout (GET /logout)
    @Test
    public void testLogout() throws Exception {
        // Configurar la sesión con un usuario autenticado
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        session.setAttribute("cliente", cliente);
        session.setAttribute("usuario", cliente);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/logout")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que la sesión se invalidó creando una nueva solicitud
        MockHttpSession newSession = new MockHttpSession();
        mockMvc.perform(get("/some-endpoint")
                        .session(newSession))
                .andExpect(request().sessionAttributeDoesNotExist("usuario"))
                .andExpect(request().sessionAttributeDoesNotExist("cliente"));
    }
}