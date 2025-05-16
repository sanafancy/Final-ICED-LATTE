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
import proyecto.iso2.persistencia.ClienteDAO;
import proyecto.iso2.persistencia.RepartidorDAO;
import proyecto.iso2.persistencia.RestauranteDAO;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RegistroControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private RestauranteDAO restauranteDAO;

    @Mock
    private RepartidorDAO repartidorDAO;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        clienteDAO = mock(ClienteDAO.class);
        restauranteDAO = mock(RestauranteDAO.class);
        repartidorDAO = mock(RepartidorDAO.class);

        // Crear una instancia del controlador
        RegistroController controller = new RegistroController();

        // Usar reflexión para inyectar los mocks en los campos privados
        injectMock(controller, "clienteDAO", clienteDAO);
        injectMock(controller, "restauranteDAO", restauranteDAO);
        injectMock(controller, "repartidorDAO", repartidorDAO);

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
    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, Direccion direccion) {
        return new Restaurante(email, pass, nombre, cif, direccion);
    }

    // Método auxiliar para crear un repartidor con datos válidos
    private Repartidor crearRepartidor(String nombre, String apellidos, String email, String pass, String nif, int eficiencia) {
        return new Repartidor(email, pass, nombre, apellidos, nif, eficiencia);
    }

    // Pruebas para el método showRegistroPage (GET /registro)
    @Test
    public void testShowRegistroPage() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"));
    }

    // Pruebas para el método showRegistroClientePage (GET /registro/cliente)
    @Test
    public void testShowRegistroClientePage() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/registro/cliente"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroCliente"));
    }

    // Pruebas para el método registrarCliente (POST /registro/cliente)
    @Test
    public void testRegistrarCliente() throws Exception {
        // Datos de prueba
        String email = "cliente@ejemplo.com";
        String pass = "pass123";
        String nombre = "Cliente";
        String apellidos = "Uno";
        String dni = "12345678A";

        // Configurar el mock para el método save
        Cliente cliente = crearCliente(nombre, apellidos, email, pass, dni);
        when(clienteDAO.save(any(Cliente.class))).thenReturn(cliente);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/registro/cliente")
                        .param("email", email)
                        .param("pass", pass)
                        .param("nombre", nombre)
                        .param("apellidos", apellidos)
                        .param("dni", dni))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que se llamó al método save con un cliente correcto
        verify(clienteDAO, times(1)).save(any(Cliente.class));
    }

    // Pruebas para el método showRegistroRestaurantePage (GET /registro/restaurante)
    @Test
    public void testShowRegistroRestaurantePage() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/registro/restaurante"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroRestaurante"));
    }

    // Pruebas para el método registrarRestaurante (POST /registro/restaurante)
    @Test
    public void testRegistrarRestaurante() throws Exception {
        // Datos de prueba
        String email = "restaurante@ejemplo.com";
        String pass = "pass123";
        String nombre = "Restaurante A";
        String cif = "CIF1";

        // Configurar el mock para el método save
        Direccion direccion = new Direccion();
        Restaurante restaurante = crearRestaurante(nombre, email, pass, cif, direccion);
        when(restauranteDAO.save(any(Restaurante.class))).thenReturn(restaurante);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/registro/restaurante")
                        .param("email", email)
                        .param("pass", pass)
                        .param("nombre", nombre)
                        .param("cif", cif))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que se llamó al método save con un restaurante correcto
        verify(restauranteDAO, times(1)).save(any(Restaurante.class));
    }

    // Pruebas para el método showRegistroRepartidorPage (GET /registro/repartidor)
    @Test
    public void testShowRegistroRepartidorPage() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/registro/repartidor"))
                .andExpect(status().isOk())
                .andExpect(view().name("registroRepartidor"));
    }

    // Pruebas para el método registrarRepartidor (POST /registro/repartidor)
    @Test
    public void testRegistrarRepartidor() throws Exception {
        // Datos de prueba
        String email = "repartidor@ejemplo.com";
        String pass = "pass123";
        String nombre = "Repartidor";
        String apellidos = "Uno";
        String nif = "12345678B";
        int eficiencia = 0; // Ajustado para coincidir con el controlador

        // Configurar el mock para el método save
        Repartidor repartidor = crearRepartidor(nombre, apellidos, email, pass, nif, eficiencia);
        when(repartidorDAO.save(any(Repartidor.class))).thenReturn(repartidor);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/registro/repartidor")
                        .param("email", email)
                        .param("pass", pass)
                        .param("nombre", nombre)
                        .param("apellidos", apellidos)
                        .param("nif", nif))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que se llamó al método save con un repartidor correcto
        verify(repartidorDAO, times(1)).save(any(Repartidor.class));
    }
}