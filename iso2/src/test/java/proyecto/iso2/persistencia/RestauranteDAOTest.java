package proyecto.iso2.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestauranteDAOTest {

    @Mock
    private RestauranteDAO restauranteDAO;

    private Restaurante restaurante1;
    private Restaurante restaurante2;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Crear datos de prueba
        Direccion direccion1 = new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
        restaurante1 = new Restaurante("restaurante1@ejemplo.com", "pass123", "Restaurante A", "CIF1", direccion1);
        setField(restaurante1, "idUsuario", 1L); // Establecer el ID usando reflexión

        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        restaurante2 = new Restaurante("restaurante2@ejemplo.com", "pass456", "Mi Rest", "CIF2", direccion2);
        setField(restaurante2, "idUsuario", 2L);
    }

    @Test
    public void testFindById_Exists() {
        // Configurar el mock
        when(restauranteDAO.findById(1L)).thenReturn(Optional.of(restaurante1));

        // Ejecutar el método
        Optional<Restaurante> result = restauranteDAO.findById(1L);

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals("Restaurante A", result.get().getNombre());
        assertEquals("CIF1", result.get().getCif());
        assertEquals("Calle Falsa", result.get().getDireccion().getCalle());
    }

    @Test
    public void testFindById_NotExists() {
        // Configurar el mock
        when(restauranteDAO.findById(999L)).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<Restaurante> result = restauranteDAO.findById(999L);

        // Verificar el resultado
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave() {
        // Datos de prueba
        Direccion direccion = new Direccion("Calle Nueva", 789, "", 28003, "Madrid");
        Restaurante restaurante = new Restaurante("nuevo@ejemplo.com", "pass789", "Restaurante Nuevo", "CIF3", direccion);

        // Configurar el mock
        when(restauranteDAO.save(any(Restaurante.class))).thenAnswer(invocation -> {
            Restaurante savedRestaurante = invocation.getArgument(0);
            setField(savedRestaurante, "idUsuario", 3L); // Simular que la base de datos asigna un ID
            return savedRestaurante;
        });

        // Ejecutar el método
        Restaurante result = restauranteDAO.save(restaurante);

        // Verificar el resultado
        assertNotNull(result.getIdUsuario());
        assertEquals(3L, result.getIdUsuario());
        assertEquals("Restaurante Nuevo", result.getNombre());
        assertEquals("CIF3", result.getCif());
        assertEquals("Calle Nueva", result.getDireccion().getCalle());
    }

    @Test
    public void testFindAll() {
        // Configurar el mock
        List<Restaurante> restaurantes = Arrays.asList(restaurante1, restaurante2);
        when(restauranteDAO.findAll()).thenReturn(restaurantes);

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findAll();

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Restaurante A", result.get(0).getNombre());
        assertEquals("Mi Rest", result.get(1).getNombre());
    }

    @Test
    public void testDeleteById() {
        // Ejecutar el método
        restauranteDAO.deleteById(1L);

        // Verificar que se llamó al método deleteById
        verify(restauranteDAO, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByNombreContainingIgnoreCase() {
        // Configurar el mock
        List<Restaurante> restaurantes = Arrays.asList(restaurante1, restaurante2);
        when(restauranteDAO.findByNombreContainingIgnoreCase("rest")).thenReturn(restaurantes);

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByNombreContainingIgnoreCase("rest");

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Restaurante A", result.get(0).getNombre());
        assertEquals("Mi Rest", result.get(1).getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_NoMatches() {
        // Configurar el mock
        when(restauranteDAO.findByNombreContainingIgnoreCase("xyz")).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByNombreContainingIgnoreCase("xyz");

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByDireccion_CalleContainingIgnoreCase() {
        // Configurar el mock
        List<Restaurante> restaurantes = Arrays.asList(restaurante1);
        when(restauranteDAO.findByDireccion_CalleContainingIgnoreCase("falsa")).thenReturn(restaurantes);

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_CalleContainingIgnoreCase("falsa");

        // Verificar el resultado
        assertEquals(1, result.size());
        assertEquals("Restaurante A", result.get(0).getNombre());
        assertEquals("Calle Falsa", result.get(0).getDireccion().getCalle());
    }

    @Test
    public void testFindByDireccion_CalleContainingIgnoreCase_NoMatches() {
        // Configurar el mock
        when(restauranteDAO.findByDireccion_CalleContainingIgnoreCase("inexistente")).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_CalleContainingIgnoreCase("inexistente");

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByDireccion_MunicipioContainingIgnoreCase() {
        // Configurar el mock
        List<Restaurante> restaurantes = Arrays.asList(restaurante1, restaurante2);
        when(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("madrid")).thenReturn(restaurantes);

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("madrid");

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("Madrid", result.get(0).getDireccion().getMunicipio());
        assertEquals("Madrid Centro", result.get(1).getDireccion().getMunicipio());
    }

    @Test
    public void testFindByDireccion_MunicipioContainingIgnoreCase_NoMatches() {
        // Configurar el mock
        when(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("barcelona")).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("barcelona");

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByDireccion_CodigoPostal() {
        // Configurar el mock
        List<Restaurante> restaurantes = Arrays.asList(restaurante1);
        when(restauranteDAO.findByDireccion_CodigoPostal(28001)).thenReturn(restaurantes);

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_CodigoPostal(28001);

        // Verificar el resultado
        assertEquals(1, result.size());
        assertEquals("Restaurante A", result.get(0).getNombre());
        assertEquals(28001, result.get(0).getDireccion().getCodigoPostal());
    }

    @Test
    public void testFindByDireccion_CodigoPostal_NoMatches() {
        // Configurar el mock
        when(restauranteDAO.findByDireccion_CodigoPostal(99999)).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Restaurante> result = restauranteDAO.findByDireccion_CodigoPostal(99999);

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    // Método auxiliar para establecer el ID usando reflexión
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error al establecer el campo " + fieldName, e);
        }
    }
}