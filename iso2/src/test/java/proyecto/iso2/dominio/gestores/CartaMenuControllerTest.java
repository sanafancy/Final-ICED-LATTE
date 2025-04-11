package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CartaMenuControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartaMenuDAO cartaMenuDAO;

    @Mock
    private ItemMenuDAO itemMenuDAO;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializar los mocks
        cartaMenuDAO = mock(CartaMenuDAO.class);
        itemMenuDAO = mock(ItemMenuDAO.class);

        // Crear una instancia del controlador
        CartaMenuController controller = new CartaMenuController();

        // Usar reflexión para inyectar los mocks en los campos privados
        injectMock(controller, "cartaMenuDAO", cartaMenuDAO);
        injectMock(controller, "itemMenuDAO", itemMenuDAO);

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

    // Métodos auxiliares para crear entidades
    private Restaurante crearRestaurante(String nombre, String email, String pass, String cif, Direccion direccion) {
        Restaurante restaurante = new Restaurante(email, pass, nombre, cif, direccion);
        // Usar reflexión para establecer el ID, ya que no hay setIdUsuario
        try {
            // Buscar el campo "idUsuario" en la clase base (Usuario)
            Field idField = restaurante.getClass().getSuperclass().getDeclaredField("idUsuario");
            idField.setAccessible(true);
            idField.set(restaurante, 1L);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el ID del restaurante", e);
        }
        return restaurante;
    }

    private Direccion crearDireccion() {
        return new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
    }

    private CartaMenu crearCartaMenu(Long id, String nombre, Restaurante restaurante) {
        CartaMenu carta = new CartaMenu(nombre, restaurante);
        // Usar reflexión para establecer el ID, ya que no hay setId
        try {
            Field idField = CartaMenu.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(carta, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el ID de la carta", e);
        }
        return carta;
    }

    private ItemMenu crearItemMenu(Long id, String nombre, double precio, CartaMenu cartaMenu) {
        ItemMenu item = new ItemMenu();
        item.setNombre(nombre);
        item.setPrecio(precio);
        item.setCartaMenu(cartaMenu);
        // Usar reflexión para establecer el ID, ya que no hay setId
        try {
            Field idField = ItemMenu.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(item, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el ID del ítem", e);
        }
        return item;
    }

    // Pruebas para el método mostrarFormularioCreacion (GET /cartas/crear)
    @Test
    public void testMostrarFormularioCreacion() throws Exception {
        // Ejecutar la solicitud GET
        mockMvc.perform(get("/cartas/crear"))
                .andExpect(status().isOk())
                .andExpect(view().name("crearCarta"))
                .andExpect(model().attributeExists("carta"));
    }

    // Pruebas para el método crearCarta (POST /cartas/crear)
    @Test
    public void testCrearCarta_SinRestaurante() throws Exception {
        // Ejecutar la solicitud POST sin restaurante en la sesión
        mockMvc.perform(post("/cartas/crear")
                        .param("nombre", "Carta Nueva"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testCrearCarta_Exitoso() throws Exception {
        // Datos de prueba
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        session.setAttribute("restaurante", restaurante);

        // Configurar los mocks
        when(cartaMenuDAO.save(any(CartaMenu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/crear")
                        .param("nombre", "Carta Nueva")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));

        // Verificar que se guardó la carta
        verify(cartaMenuDAO, times(1)).save(any(CartaMenu.class));
    }

    // Pruebas para el método editarCarta (GET /cartas/editar/{id})
    @Test
    public void testEditarCarta_CartaNoExistente() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.empty());

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/cartas/editar/{id}", cartaId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cartas"));
    }

    @Test
    public void testEditarCarta_Exitoso() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        CartaMenu carta = crearCartaMenu(cartaId, "Carta Existente", restaurante);
        List<ItemMenu> items = Arrays.asList(
                crearItemMenu(1L, "Item 1", 10.0, carta),
                crearItemMenu(2L, "Item 2", 15.0, carta)
        );
        carta.setItems(items);

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.of(carta));

        // Ejecutar la solicitud GET
        mockMvc.perform(get("/cartas/editar/{id}", cartaId))
                .andExpect(status().isOk())
                .andExpect(view().name("editarCarta"))
                .andExpect(model().attribute("carta", carta))
                .andExpect(model().attribute("items", items))
                .andExpect(model().attributeExists("itemNuevo"));
    }

    // Pruebas para el método actualizarCarta (POST /cartas/editar/{id})
    @Test
    public void testActualizarCarta_CartaNoExistente() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.empty());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/editar/{id}", cartaId)
                        .param("nombre", "Carta Actualizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));
    }

    @Test
    public void testActualizarCarta_Exitoso() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        CartaMenu carta = crearCartaMenu(cartaId, "Carta Existente", restaurante);

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.of(carta));
        when(cartaMenuDAO.save(any(CartaMenu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/editar/{id}", cartaId)
                        .param("nombre", "Carta Actualizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cartas/editar/" + cartaId));

        // Verificar que se actualizó el nombre y se guardó
        verify(cartaMenuDAO, times(1)).save(carta);
        assert carta.getNombre().equals("Carta Actualizada");
    }

    // Pruebas para el método eliminarCarta (POST /cartas/eliminar/{id})
    @Test
    public void testEliminarCarta() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/eliminar/{id}", cartaId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));

        // Verificar que se eliminó la carta
        verify(cartaMenuDAO, times(1)).deleteById(cartaId);
    }

    // Pruebas para el método agregarItem (POST /cartas/editar/{cartaId}/agregarItem)
    @Test
    public void testAgregarItem_CartaNoExistente() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.empty());

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/editar/{cartaId}/agregarItem", cartaId)
                        .param("nombre", "Item Nuevo")
                        .param("precio", "10.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inicioRestaurante"));
    }

    @Test
    public void testAgregarItem_Exitoso() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;
        Restaurante restaurante = crearRestaurante("Restaurante A", "restaurante@ejemplo.com", "pass123", "CIF1", new Direccion());
        CartaMenu carta = crearCartaMenu(cartaId, "Carta Existente", restaurante);

        // Configurar los mocks
        when(cartaMenuDAO.findById(cartaId)).thenReturn(Optional.of(carta));
        when(itemMenuDAO.save(any(ItemMenu.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/editar/{cartaId}/agregarItem", cartaId)
                        .param("nombre", "Item Nuevo")
                        .param("precio", "10.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cartas/editar/" + cartaId));

        // Verificar que se guardó el ítem
        verify(itemMenuDAO, times(1)).save(any(ItemMenu.class));
    }

    // Pruebas para el método eliminarItem (POST /cartas/editar/{cartaId}/eliminarItem/{itemId})
    @Test
    public void testEliminarItem() throws Exception {
        // Datos de prueba
        Long cartaId = 1L;
        Long itemId = 2L;

        // Ejecutar la solicitud POST
        mockMvc.perform(post("/cartas/editar/{cartaId}/eliminarItem/{itemId}", cartaId, itemId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cartas/editar/" + cartaId));

        // Verificar que se eliminó el ítem
        verify(itemMenuDAO, times(1)).deleteById(itemId);
    }
}