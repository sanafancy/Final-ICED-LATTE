package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;
import org.springframework.mock.web.MockHttpSession;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RestauranteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RestauranteDAO restauranteDAO;

    @Mock
    private CartaMenuDAO cartaMenuDAO;

    @Mock
    private ItemMenuDAO itemMenuDAO;

    @Mock
    private DireccionDAO direccionDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        restauranteDAO = mock(RestauranteDAO.class);
        cartaMenuDAO = mock(CartaMenuDAO.class);
        itemMenuDAO = mock(ItemMenuDAO.class);
        direccionDAO = mock(DireccionDAO.class);
        usuarioDAO = mock(UsuarioDAO.class);

        // Crear una instancia del controlador
        RestauranteController controller = new RestauranteController();

        // Usar reflexión para inyectar los mocks en los campos privados
        injectMock(controller, "restauranteDAO", restauranteDAO);
        injectMock(controller, "cartaMenuDAO", cartaMenuDAO);
        injectMock(controller, "itemMenuDAO", itemMenuDAO);
        injectMock(controller, "direccionDAO", direccionDAO);
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

    // Método auxiliar para crear un restaurante con datos válidos
    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, String calle, String municipio, int codigoPostal) {
        Direccion direccion = new Direccion();
        direccion.setCalle(calle);
        direccion.setMunicipio(municipio);
        direccion.setCodigoPostal(codigoPostal);
        return new Restaurante(email, pass, nombre, cif, direccion);
    }

    // Método auxiliar para crear un cliente con datos válidos
    private Cliente crearCliente(String nombre, String apellidos, String email, String pass, String dni) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        cliente.setDirecciones(new ArrayList<>()); // Inicializar direcciones para evitar NullPointerException
        return cliente;
    }

    // Pruebas para el método home (GET /)
    @Test
    public void testHome_ClienteAutenticado() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante1 = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        Restaurante restaurante2 = crearRestaurante("Restaurante B", "restaurante2@ejemplo.com", "pass2", "CIF2", "Avenida 456", "Barcelona", 38001);
        when(restauranteDAO.findAll()).thenReturn(Arrays.asList(restaurante1, restaurante2));

        // Configurar la sesión con un cliente autenticado
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("restaurantes", Arrays.asList(restaurante1, restaurante2)))
                .andExpect(model().attribute("cliente", cliente));
    }

    @Test
    public void testHome_SinClienteAutenticado() throws Exception {
        // Datos de prueba
        Restaurante restaurante1 = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        Restaurante restaurante2 = crearRestaurante("Restaurante B", "restaurante2@ejemplo.com", "pass2", "CIF2", "Avenida 456", "Barcelona", 38001);
        when(restauranteDAO.findAll()).thenReturn(Arrays.asList(restaurante1, restaurante2));

        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("restaurantes", Arrays.asList(restaurante1, restaurante2)))
                .andExpect(model().attributeDoesNotExist("cliente"));
    }

    // Pruebas para el método buscarRestaurante (GET /buscarRestaurante)
    @Test
    public void testBuscarRestaurante_ConBusquedaPorNombre() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        when(restauranteDAO.findByNombreContainingIgnoreCase("A")).thenReturn(Arrays.asList(restaurante));
        when(restauranteDAO.findByDireccion_CalleContainingIgnoreCase("A")).thenReturn(new ArrayList<>());
        when(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("A")).thenReturn(new ArrayList<>());
        when(restauranteDAO.findByDireccion_CodigoPostal(anyInt())).thenReturn(new ArrayList<>());

        // Ejecutar la solicitud GET con un término de búsqueda
        mockMvc.perform(get("/buscarRestaurante")
                        .param("busqueda", "A"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("restaurantes", Arrays.asList(restaurante)));
    }

    @Test
    public void testBuscarRestaurante_ConBusquedaPorCodigoPostal() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        when(restauranteDAO.findByNombreContainingIgnoreCase("28001")).thenReturn(new ArrayList<>());
        when(restauranteDAO.findByDireccion_CalleContainingIgnoreCase("28001")).thenReturn(new ArrayList<>());
        when(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("28001")).thenReturn(new ArrayList<>());
        when(restauranteDAO.findByDireccion_CodigoPostal(28001)).thenReturn(Arrays.asList(restaurante));

        // Ejecutar la solicitud GET con un término de búsqueda que es un código postal
        mockMvc.perform(get("/buscarRestaurante")
                        .param("busqueda", "28001"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("restaurantes", Arrays.asList(restaurante)));
    }

    @Test
    public void testBuscarRestaurante_SinBusqueda() throws Exception {
        // Datos de prueba
        Restaurante restaurante1 = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        Restaurante restaurante2 = crearRestaurante("Restaurante B", "restaurante2@ejemplo.com", "pass2", "CIF2", "Avenida 456", "Barcelona", 38001);
        when(restauranteDAO.findAll()).thenReturn(Arrays.asList(restaurante1, restaurante2));

        // Ejecutar la solicitud GET sin término de búsqueda
        mockMvc.perform(get("/buscarRestaurante"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("restaurantes", Arrays.asList(restaurante1, restaurante2)));
    }

    // Pruebas para el método inicioRestaurante (GET /inicioRestaurante)
    @Test
    public void testInicioRestaurante_RestauranteAutenticado() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta 1");
        when(cartaMenuDAO.findByRestaurante(restaurante)).thenReturn(Arrays.asList(carta));

        // Configurar la sesión con un restaurante autenticado
        session.setAttribute("restaurante", restaurante);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/inicioRestaurante")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("inicioRestaurante"))
                .andExpect(model().attribute("restaurante", restaurante))
                .andExpect(model().attribute("cartas", Arrays.asList(carta)));
    }

    @Test
    public void testInicioRestaurante_SinRestauranteAutenticado() throws Exception {
        // Ejecutar la solicitud GET sin restaurante en la sesión
        mockMvc.perform(get("/inicioRestaurante"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // Pruebas para el método toggleFavorito (POST /favorito/{id})
    @Test
    public void testToggleFavorito_AgregarFavorito() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        when(restauranteDAO.findById(1L)).thenReturn(Optional.of(restaurante));
        when(usuarioDAO.save(any(Cliente.class))).thenReturn(cliente);

        // Configurar el ID del restaurante usando reflexión (ya que no hay setId)
        Field idField = Usuario.class.getDeclaredField("idUsuario");
        idField.setAccessible(true);
        idField.set(restaurante, 1L);

        // Configurar la sesión con un cliente autenticado
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/favorito/1")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que se llamó al método save
        verify(usuarioDAO, times(1)).save(cliente);
    }

    @Test
    public void testToggleFavorito_SinClienteAutenticado() throws Exception {
        // Ejecutar la solicitud POST sin cliente en la sesión
        mockMvc.perform(post("/favorito/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // Pruebas para el método verFavoritos (GET /restaurantes/favoritos)
    @Test
    public void testVerFavoritos_ClienteAutenticado() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente("Cliente", "Uno", "cliente@ejemplo.com", "pass123", "12345678A");
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        cliente.getFavoritos().add(restaurante);

        // Configurar la sesión con un cliente autenticado
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/restaurantes/favoritos")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("favoritos"))
                .andExpect(model().attribute("favoritos", Arrays.asList(restaurante)));
    }

    @Test
    public void testVerFavoritos_SinClienteAutenticado() throws Exception {
        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/restaurantes/favoritos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // Pruebas para el método eliminarRestaurante (POST /eliminarRestaurante)
    @Test
    public void testEliminarRestaurante_RestauranteAutenticado() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);

        // Configurar la sesión con un restaurante autenticado
        session.setAttribute("restaurante", restaurante);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/eliminarRestaurante")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Verificar que se llamó al método delete
        verify(restauranteDAO, times(1)).delete(restaurante);
    }

    @Test
    public void testEliminarRestaurante_SinRestauranteAutenticado() throws Exception {
        // Ejecutar la solicitud POST sin restaurante en la sesión
        mockMvc.perform(post("/eliminarRestaurante"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // Pruebas para el método verMenuRestaurante (GET /restaurante/{id})
    @Test
    public void testVerMenuRestaurante_RestauranteExistente() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante1@ejemplo.com", "pass1", "CIF1", "Calle 123", "Madrid", 28001);
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta 1");
        ItemMenu item = new ItemMenu();
        item.setNombre("Item 1");
        item.setPrecio(10.0);
        when(restauranteDAO.findById(1L)).thenReturn(Optional.of(restaurante));
        when(cartaMenuDAO.findByRestaurante(restaurante)).thenReturn(Arrays.asList(carta));
        when(itemMenuDAO.findByCartaMenu(carta)).thenReturn(Arrays.asList(item));

        // Configurar el ID del restaurante usando reflexión
        Field idField = Usuario.class.getDeclaredField("idUsuario");
        idField.setAccessible(true);
        idField.set(restaurante, 1L);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/restaurante/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("verMenus"))
                .andExpect(model().attribute("restaurante", restaurante))
                .andExpect(model().attribute("cartas", Arrays.asList(carta)));
    }

    @Test
    public void testVerMenuRestaurante_RestauranteNoExistente() throws Exception {
        // Configurar el mock para que el restaurante no exista
        when(restauranteDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/restaurante/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}