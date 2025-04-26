package proyecto.iso2.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.dominio.entidades.Usuario;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioDAOTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    private Restaurante restaurante;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Crear un usuario (Restaurante) para las pruebas
        Direccion direccion = new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
        restaurante = new Restaurante("restaurante@ejemplo.com", "pass123", "Restaurante A", "CIF1", direccion);
        setField(restaurante, "idUsuario", 1L);
    }

    @Test
    public void testFindById_Exists() {
        // Configurar el mock
        when(usuarioDAO.findById(1L)).thenReturn(Optional.of(restaurante));

        // Ejecutar el método
        Optional<Usuario> result = usuarioDAO.findById(1L);

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals("restaurante@ejemplo.com", result.get().getEmail());
        assertEquals("pass123", result.get().getPass());
    }

    @Test
    public void testFindById_NotExists() {
        // Configurar el mock
        when(usuarioDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<Usuario> result = usuarioDAO.findById(1L);

        // Verificar el resultado
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave() {
        // Datos de prueba
        Direccion direccion = new Direccion("Calle Nueva", 789, "", 28003, "Madrid");
        Restaurante nuevoRestaurante = new Restaurante("nuevo@ejemplo.com", "pass789", "Restaurante Nuevo", "CIF3", direccion);

        // Configurar el mock
        when(usuarioDAO.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario savedUsuario = invocation.getArgument(0);
            setField(savedUsuario, "idUsuario", 2L);
            return savedUsuario;
        });

        // Ejecutar el método
        Usuario result = usuarioDAO.save(nuevoRestaurante);

        // Verificar el resultado
        assertNotNull(result.getIdUsuario());
        assertEquals(2L, result.getIdUsuario());
        assertEquals("nuevo@ejemplo.com", result.getEmail());
        assertEquals("pass789", result.getPass());
    }

    @Test
    public void testFindAll() {
        // Datos de prueba
        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        Restaurante restaurante2 = new Restaurante("restaurante2@ejemplo.com", "pass456", "Restaurante B", "CIF2", direccion2);
        setField(restaurante2, "idUsuario", 2L);
        List<Usuario> usuarios = Arrays.asList(restaurante, restaurante2);

        // Configurar el mock
        when(usuarioDAO.findAll()).thenReturn(usuarios);

        // Ejecutar el método
        List<Usuario> result = usuarioDAO.findAll();

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals("restaurante@ejemplo.com", result.get(0).getEmail());
        assertEquals("restaurante2@ejemplo.com", result.get(1).getEmail());
    }

    @Test
    public void testDeleteById() {
        // Ejecutar el método
        usuarioDAO.deleteById(1L);

        // Verificar que se llamó al método deleteById
        verify(usuarioDAO, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByEmailAndPass_Exists() {
        // Configurar el mock
        when(usuarioDAO.findByEmailAndPass("restaurante@ejemplo.com", "pass123")).thenReturn(Optional.of(restaurante));

        // Ejecutar el método
        Optional<Usuario> result = usuarioDAO.findByEmailAndPass("restaurante@ejemplo.com", "pass123");

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals("restaurante@ejemplo.com", result.get().getEmail());
        assertEquals("pass123", result.get().getPass());
    }

    @Test
    public void testFindByEmailAndPass_NotExists() {
        // Configurar el mock
        when(usuarioDAO.findByEmailAndPass("noexistente@ejemplo.com", "pass123")).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<Usuario> result = usuarioDAO.findByEmailAndPass("noexistente@ejemplo.com", "pass123");

        // Verificar el resultado
        assertFalse(result.isPresent());
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