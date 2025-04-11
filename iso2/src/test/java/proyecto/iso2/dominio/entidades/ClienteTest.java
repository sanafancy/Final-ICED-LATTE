package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ClienteTest {

    private Cliente cliente;
    private Restaurante restaurante;
    private Direccion direccion;

    @BeforeEach
    public void setUp() {
        // Inicializamos una dirección para el restaurante usando el constructor con parámetros
        direccion = new Direccion("Calle Falsa", 123, "Piso 1", 28001, "Madrid");

        // Inicializamos un restaurante usando el constructor con parámetros
        restaurante = new Restaurante("restaurante@email.com", "pass123", "Pizzería Bella", "B12345678", direccion);

        // Inicializamos un cliente para cada prueba
        cliente = new Cliente("juan@email.com", "password123", "Juan", "Pérez", "12345678A");
    }

    @Test
    public void testConstructorConParametros() {
        // Verifica que los campos se inicializan correctamente
        assertEquals("juan@email.com", cliente.getEmail());
        assertEquals("password123", cliente.getPass());
        assertEquals("Juan", cliente.getNombre());
        assertEquals("Pérez", cliente.getApellidos());
        assertEquals("12345678A", cliente.getDni());
        assertNotNull(cliente.getDirecciones());
        assertTrue(cliente.getDirecciones().isEmpty());
        assertNotNull(cliente.getFavoritos());
        assertTrue(cliente.getFavoritos().isEmpty());
    }

    @Test
    public void testGetFavoritosInicializaLista() {
        // Forzamos que favoritos sea null
        cliente.setFavoritos(null);
        List<Restaurante> favoritos = cliente.getFavoritos();
        assertNotNull(favoritos);
        assertTrue(favoritos.isEmpty());
    }

    @Test
    public void testSetFavoritosConNull() {
        // Pasamos null y verificamos que se asigna una lista vacía
        cliente.setFavoritos(null);
        assertNotNull(cliente.getFavoritos());
        assertTrue(cliente.getFavoritos().isEmpty());
    }

    @Test
    public void testSetFavoritosConListaNoVacia() {
        // Creamos una lista con un restaurante
        List<Restaurante> favoritos = new ArrayList<>();
        favoritos.add(restaurante);

        // Asignamos la lista y verificamos
        cliente.setFavoritos(favoritos);
        assertEquals(1, cliente.getFavoritos().size());
        assertEquals("Pizzería Bella", cliente.getFavoritos().get(0).getNombre());
    }

    @Test
    public void testToString() {
        String expected = "Cliente [idUsuario=null, pass=password123, nombre=Juan, apellidos=Pérez, dni=12345678A]";
        assertEquals(expected, cliente.toString());
    }
}