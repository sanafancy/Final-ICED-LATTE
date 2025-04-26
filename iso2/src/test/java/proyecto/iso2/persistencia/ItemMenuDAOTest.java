package proyecto.iso2.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.ItemMenu;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemMenuDAOTest {

    @Mock
    private ItemMenuDAO itemMenuDAO;

    private CartaMenu carta;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Crear una carta para las pruebas
        Direccion direccion = new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
        Restaurante restaurante = new Restaurante("restaurante@ejemplo.com", "pass123", "Restaurante A", "CIF1", direccion);
        setField(restaurante, "idUsuario", 1L);
        carta = new CartaMenu("Carta 1", restaurante);
        setField(carta, "id", 1L);
    }

    @Test
    public void testFindById_Exists() {
        // Datos de prueba
        ItemMenu item = new ItemMenu();
        item.setNombre("Item 1");
        item.setPrecio(10.0);
        item.setCartaMenu(carta);
        setField(item, "id", 1L);

        // Configurar el mock
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.of(item));

        // Ejecutar el método
        Optional<ItemMenu> result = itemMenuDAO.findById(1L);

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals("Item 1", result.get().getNombre());
        assertEquals(10.0, result.get().getPrecio(), 0.01);
        assertEquals(carta, result.get().getCartaMenu());
    }

    @Test
    public void testFindById_NotExists() {
        // Configurar el mock
        when(itemMenuDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<ItemMenu> result = itemMenuDAO.findById(1L);

        // Verificar el resultado
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave() {
        // Datos de prueba
        ItemMenu item = new ItemMenu();
        item.setNombre("Item 1");
        item.setPrecio(10.0);
        item.setCartaMenu(carta);

        // Configurar el mock
        when(itemMenuDAO.save(any(ItemMenu.class))).thenAnswer(invocation -> {
            ItemMenu savedItem = invocation.getArgument(0);
            setField(savedItem, "id", 1L);
            return savedItem;
        });

        // Ejecutar el método
        ItemMenu result = itemMenuDAO.save(item);

        // Verificar el resultado
        assertNotNull(result.getId());
        assertEquals(1L, result.getId());
        assertEquals("Item 1", result.getNombre());
        assertEquals(10.0, result.getPrecio(), 0.01);
        assertEquals(carta, result.getCartaMenu()); // Corrección: eliminamos el .get()
    }

    @Test
    public void testFindAll() {
        // Datos de prueba
        ItemMenu item1 = new ItemMenu();
        item1.setNombre("Item 1");
        item1.setPrecio(10.0);
        item1.setCartaMenu(carta);
        setField(item1, "id", 1L);

        ItemMenu item2 = new ItemMenu();
        item2.setNombre("Item 2");
        item2.setPrecio(15.0);
        item2.setCartaMenu(carta);
        setField(item2, "id", 2L);

        List<ItemMenu> items = Arrays.asList(item1, item2);

        // Configurar el mock
        when(itemMenuDAO.findAll()).thenReturn(items);

        // Ejecutar el método
        List<ItemMenu> result = itemMenuDAO.findAll();

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Item 1", result.get(0).getNombre());
        assertEquals(10.0, result.get(0).getPrecio(), 0.01);
        assertEquals("Item 2", result.get(1).getNombre());
        assertEquals(15.0, result.get(1).getPrecio(), 0.01);
    }

    @Test
    public void testDeleteById() {
        // Ejecutar el método
        itemMenuDAO.deleteById(1L);

        // Verificar que se llamó al método deleteById
        verify(itemMenuDAO, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByCartaMenu() {
        // Datos de prueba
        ItemMenu item1 = new ItemMenu();
        item1.setNombre("Item 1");
        item1.setPrecio(10.0);
        item1.setCartaMenu(carta);
        setField(item1, "id", 1L);

        ItemMenu item2 = new ItemMenu();
        item2.setNombre("Item 2");
        item2.setPrecio(15.0);
        item2.setCartaMenu(carta);
        setField(item2, "id", 2L);

        List<ItemMenu> items = Arrays.asList(item1, item2);

        // Configurar el mock
        when(itemMenuDAO.findByCartaMenu(carta)).thenReturn(items);

        // Ejecutar el método
        List<ItemMenu> result = itemMenuDAO.findByCartaMenu(carta);

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Item 1", result.get(0).getNombre());
        assertEquals(10.0, result.get(0).getPrecio(), 0.01);
        assertEquals("Item 2", result.get(1).getNombre());
        assertEquals(15.0, result.get(1).getPrecio(), 0.01);
        assertEquals(carta, result.get(0).getCartaMenu());
    }

    @Test
    public void testFindByCartaMenu_NoMatches() {
        // Crear una carta diferente
        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        Restaurante restaurante2 = new Restaurante("restaurante2@ejemplo.com", "pass456", "Restaurante B", "CIF2", direccion2);
        setField(restaurante2, "idUsuario", 2L);
        CartaMenu carta2 = new CartaMenu("Carta 2", restaurante2);
        setField(carta2, "id", 2L);

        // Configurar el mock
        when(itemMenuDAO.findByCartaMenu(carta2)).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<ItemMenu> result = itemMenuDAO.findByCartaMenu(carta2);

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    // Método auxiliar para establecer el ID usando reflexión
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field;
            if (fieldName.equals("idUsuario")) {
                field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            } else {
                field = target.getClass().getDeclaredField(fieldName);
            }
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el campo " + fieldName, e);
        }
    }
}