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
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.ClienteDAO;
import proyecto.iso2.persistencia.DireccionDAO;
import proyecto.iso2.persistencia.RestauranteDAO;
import java.lang.reflect.Method;
import java.util.Arrays;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DireccionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DireccionDAO direccionDAO;

    @Mock
    private RestauranteDAO restauranteDAO;

    @Mock
    private ClienteDAO clienteDAO;

    @InjectMocks
    private DireccionController direccionController;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

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
        mockMvc = MockMvcBuilders.standaloneSetup(direccionController)
                .setViewResolvers(viewResolver)
                .build();

        // Inicializar la sesión
        session = new MockHttpSession();
    }

    // Métodos auxiliares para crear entidades
    private Cliente crearCliente() {
        Cliente cliente = new Cliente("cliente@email.com", "pass123", "Juan", "Pérez", "12345678A");
        setIdUsuario(cliente, 1L);
        return cliente;
    }

    private Restaurante crearRestaurante() {
        Direccion direccion = crearDireccion();
        Restaurante restaurante = new Restaurante("restaurante@email.com", "pass123", "Pizzería Bella", "B12345678", direccion);
        setIdUsuario(restaurante, 2L);
        return restaurante;
    }

    private Direccion crearDireccion() {
        Direccion direccion = new Direccion("Calle Falsa", 123, "Piso 1", 28001, "Madrid");
        setField(direccion, "id", 1L);
        return direccion;
    }

    // Método auxiliar para establecer el ID usando setters
    private void setIdUsuario(Object target, Long id) {
        try {
            // Intentar usar el setter setIdUsuario
            Method setIdUsuario = target.getClass().getSuperclass().getDeclaredMethod("setIdUsuario", Long.class);
            setIdUsuario.invoke(target, id);
        } catch (NoSuchMethodException e) {
            try {
                // Fallback: intentar usar setId
                Method setId = target.getClass().getSuperclass().getDeclaredMethod("setId", Long.class);
                setId.invoke(target, id);
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                throw new RuntimeException("Error al establecer el ID usando setters", ex);
            }
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException("Error al invocar el setter para el ID", e);
        }
    }

    // Método auxiliar para establecer otros campos usando reflexión
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el campo " + fieldName, e);
        }
    }

    // Pruebas para GET /direcciones/añadir
    @Test
    public void testMostrarFormularioAñadir_SinCliente() throws Exception {
        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/direcciones/añadir")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMostrarFormularioAñadir_ConCliente() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/direcciones/añadir")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("añadirDireccion"))
                .andExpect(model().attributeExists("direccion"))
                .andExpect(model().attribute("direccion", is(instanceOf(Direccion.class))));
    }

    // Pruebas para POST /direcciones/añadir
    @Test
    public void testAñadirDireccion_SinCliente() throws Exception {
        // Ejecutar la solicitud POST sin cliente en la sesión
        mockMvc.perform(post("/direcciones/añadir")
                        .param("calle", "Calle Nueva")
                        .param("numero", "456")
                        .param("complemento", "Piso 2")
                        .param("codigoPostal", "28002")
                        .param("municipio", "Madrid"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testAñadirDireccion_ConCliente() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        session.setAttribute("cliente", cliente);
        Direccion direccion = crearDireccion();

        // Configurar los mocks
        when(direccionDAO.save(any(Direccion.class))).thenReturn(direccion);
        when(clienteDAO.save(any(Cliente.class))).thenReturn(cliente);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/direcciones/añadir")
                        .param("calle", "Calle Nueva")
                        .param("numero", "456")
                        .param("complemento", "Piso 2")
                        .param("codigoPostal", "28002")
                        .param("municipio", "Madrid")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/direcciones/ver"));

        // Verificar interacciones con los DAOs
        verify(direccionDAO, times(1)).save(any(Direccion.class));
        verify(clienteDAO, times(1)).save(any(Cliente.class));
    }

    // Pruebas para GET /direcciones/ver
    @Test
    public void testVerDirecciones_SinCliente() throws Exception {
        // Ejecutar la solicitud GET sin cliente en la sesión
        mockMvc.perform(get("/direcciones/ver")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testVerDirecciones_ConCliente() throws Exception {
        // Datos de prueba
        Cliente cliente = crearCliente();
        Direccion direccion = crearDireccion();
        cliente.getDirecciones().add(direccion);
        session.setAttribute("cliente", cliente);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/direcciones/ver")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("verDirecciones"))
                .andExpect(model().attribute("direcciones", cliente.getDirecciones()));
    }

    // Pruebas para GET /direcciones
    @Test
    public void testMostrarDireccion_SinRestaurante() throws Exception {
        // Ejecutar la solicitud GET sin restaurante en la sesión
        mockMvc.perform(get("/direcciones")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testMostrarDireccion_ConRestaurante_DireccionExistente() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante();
        session.setAttribute("restaurante", restaurante);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/direcciones")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("direcciones"))
                .andExpect(model().attribute("direccion", restaurante.getDireccion()));
    }

    @Test
    public void testMostrarDireccion_ConRestaurante_SinDireccion() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante();
        restaurante.setDireccion(null); // Simular que no hay dirección
        session.setAttribute("restaurante", restaurante);

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/direcciones")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("direcciones"))
                .andExpect(model().attributeExists("direccion"))
                .andExpect(model().attribute("direccion", is(instanceOf(Direccion.class))));
    }

    // Pruebas para POST /direcciones/editar
    @Test
    public void testEditarDireccion_SinRestaurante() throws Exception {
        // Ejecutar la solicitud POST sin restaurante en la sesión
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Actualizada")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("codigoPostal", "28003")
                        .param("municipio", "Barcelona"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testEditarDireccion_ConRestaurante_DireccionExistente() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante();
        session.setAttribute("restaurante", restaurante);
        Direccion direccionActualizada = new Direccion("Calle Actualizada", 789, "Piso 3", 28003, "Barcelona");
        when(direccionDAO.save(any(Direccion.class))).thenReturn(restaurante.getDireccion());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Actualizada")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("codigoPostal", "28003")
                        .param("municipio", "Barcelona")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/direcciones"));

        // Verificar que la dirección se actualizó
        verify(direccionDAO, times(1)).save(any(Direccion.class));
    }

    @Test
    public void testEditarDireccion_ConRestaurante_SinDireccion() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante();
        restaurante.setDireccion(null); // Simular que no hay dirección
        session.setAttribute("restaurante", restaurante);

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/direcciones/editar")
                        .param("calle", "Calle Actualizada")
                        .param("numero", "789")
                        .param("complemento", "Piso 3")
                        .param("codigoPostal", "28003")
                        .param("municipio", "Barcelona")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/direcciones"));

        // Verificar que no se llamó a save
        verify(direccionDAO, never()).save(any(Direccion.class));
    }
}