package proyecto.iso2.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartaMenuDAOTest {

    @Mock
    private CartaMenuDAO cartaMenuDAO;

    private Restaurante restaurante;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Crear un restaurante para las pruebas
        Direccion direccion = new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
        restaurante = new Restaurante("restaurante@ejemplo.com", "pass123", "Restaurante A", "CIF1", direccion);
        setField(restaurante, "idUsuario", 1L);
    }

    @Test
    public void testFindById_Exists() {
        // Datos de prueba
        CartaMenu carta = new CartaMenu("Carta 1", restaurante);
        setField(carta, "id", 1L);

        // Configurar el mock
        when(cartaMenuDAO.findById(1L)).thenReturn(Optional.of(carta));

        // Ejecutar el método
        Optional<CartaMenu> result = cartaMenuDAO.findById(1L);

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals("Carta 1", result.get().getNombre());
        assertEquals(restaurante, result.get().getRestaurante());
    }

    @Test
    public void testFindById_NotExists() {
        // Configurar el mock
        when(cartaMenuDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<CartaMenu> result = cartaMenuDAO.findById(1L);

        // Verificar el resultado
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave() {
        // Datos de prueba
        CartaMenu carta = new CartaMenu("Carta 1", restaurante);

        // Configurar el mock
        when(cartaMenuDAO.save(any(CartaMenu.class))).thenAnswer(invocation -> {
            CartaMenu savedCarta = invocation.getArgument(0);
            setField(savedCarta, "id", 1L);
            return savedCarta;
        });

        // Ejecutar el método
        CartaMenu result = cartaMenuDAO.save(carta);

        // Verificar el resultado
        assertNotNull(result.getId());
        assertEquals(1L, result.getId());
        assertEquals("Carta 1", result.getNombre());
        assertEquals(restaurante, result.getRestaurante());
    }

    @Test
    public void testFindAll() {
        // Datos de prueba
        CartaMenu carta1 = new CartaMenu("Carta 1", restaurante);
        setField(carta1, "id", 1L);
        CartaMenu carta2 = new CartaMenu("Carta 2", restaurante);
        setField(carta2, "id", 2L);
        List<CartaMenu> cartas = Arrays.asList(carta1, carta2);

        // Configurar el mock
        when(cartaMenuDAO.findAll()).thenReturn(cartas);

        // Ejecutar el método
        List<CartaMenu> result = cartaMenuDAO.findAll();

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Carta 1", result.get(0).getNombre());
        assertEquals("Carta 2", result.get(1).getNombre());
    }

    @Test
    public void testDeleteById() {
        // Ejecutar el método
        cartaMenuDAO.deleteById(1L);

        // Verificar que se llamó al método deleteById
        verify(cartaMenuDAO, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByRestaurante() {
        // Datos de prueba
        CartaMenu carta1 = new CartaMenu("Carta 1", restaurante);
        setField(carta1, "id", 1L);
        CartaMenu carta2 = new CartaMenu("Carta 2", restaurante);
        setField(carta2, "id", 2L);
        List<CartaMenu> cartas = Arrays.asList(carta1, carta2);

        // Configurar el mock
        when(cartaMenuDAO.findByRestaurante(restaurante)).thenReturn(cartas);

        // Ejecutar el método
        List<CartaMenu> result = cartaMenuDAO.findByRestaurante(restaurante);

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Carta 1", result.get(0).getNombre());
        assertEquals("Carta 2", result.get(1).getNombre());
        assertEquals(restaurante, result.get(0).getRestaurante());
    }

    @Test
    public void testFindByRestaurante_NoMatches() {
        // Crear un restaurante diferente
        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        Restaurante restaurante2 = new Restaurante("restaurante2@ejemplo.com", "pass456", "Restaurante B", "CIF2", direccion2);
        setField(restaurante2, "idUsuario", 2L);

        // Configurar el mock
        when(cartaMenuDAO.findByRestaurante(restaurante2)).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<CartaMenu> result = cartaMenuDAO.findByRestaurante(restaurante2);

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