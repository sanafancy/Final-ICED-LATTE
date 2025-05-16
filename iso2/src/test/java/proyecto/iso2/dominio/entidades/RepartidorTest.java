package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RepartidorTest {

    private Repartidor repartidor;

    @BeforeEach
    public void setUp() {
        // Inicializamos una instancia de Repartidor para cada prueba
        repartidor = new Repartidor();
    }

    @Test
    public void testConstructorPorDefecto() {
        // Verifica que el constructor por defecto inicializa los campos correctamente
        assertNull(repartidor.getIdUsuario(), "El ID de usuario debería ser null");
        assertNull(repartidor.getEmail(), "El email debería ser null");
        assertNull(repartidor.getPass(), "La contraseña debería ser null");
        assertNull(repartidor.getNombre(), "El nombre debería ser null");
        assertNull(repartidor.getApellidos(), "Los apellidos deberían ser null");
        assertNull(repartidor.getNif(), "El NIF debería ser null");
        assertNull(repartidor.getEficiencia(), "La eficiencia debería ser null");
    }

    @Test
    public void testConstructorConParametros() {
        // Datos de prueba
        String email = "repartidor@email.com";
        String pass = "pass123";
        String nombre = "Ana";
        String apellidos = "Gómez";
        String nif = "87654321B";
        Integer eficiencia = 95;

        // Crear una instancia usando el constructor con parámetros
        Repartidor nuevoRepartidor = new Repartidor(email, pass, nombre, apellidos, nif, eficiencia);

        // Verifica que los campos se inicializan correctamente
        assertNull(nuevoRepartidor.getIdUsuario(), "El ID de usuario debería ser null");
        assertEquals(email, nuevoRepartidor.getEmail(), "El email no coincide");
        assertEquals(pass, nuevoRepartidor.getPass(), "La contraseña no coincide");
        assertEquals(nombre, nuevoRepartidor.getNombre(), "El nombre no coincide");
        assertEquals(apellidos, nuevoRepartidor.getApellidos(), "Los apellidos no coinciden");
        assertEquals(nif, nuevoRepartidor.getNif(), "El NIF no coincide");
        assertEquals(eficiencia, nuevoRepartidor.getEficiencia(), "La eficiencia no coincide");
    }

    @Test
    public void testSettersAndGetters() {
        // Datos de prueba
        Long idUsuario = 1L;
        String email = "repartidor@email.com";
        String pass = "pass123";
        String nombre = "Luis";
        String apellidos = "Martínez";
        String nif = "12345678C";
        Integer eficiencia = 90;

        // Usar setters para establecer los valores
        repartidor.setIdUsuario(idUsuario);
        repartidor.setEmail(email);
        repartidor.setPass(pass);
        repartidor.setNombre(nombre);
        repartidor.setApellidos(apellidos);
        repartidor.setNif(nif);
        repartidor.setEficiencia(eficiencia);

        // Verifica que los getters devuelven los valores correctos
        assertEquals(idUsuario, repartidor.getIdUsuario(), "El ID de usuario no coincide");
        assertEquals(email, repartidor.getEmail(), "El email no coincide");
        assertEquals(pass, repartidor.getPass(), "La contraseña no coincide");
        assertEquals(nombre, repartidor.getNombre(), "El nombre no coincide");
        assertEquals(apellidos, repartidor.getApellidos(), "Los apellidos no coinciden");
        assertEquals(nif, repartidor.getNif(), "El NIF no coincide");
        assertEquals(eficiencia, repartidor.getEficiencia(), "La eficiencia no coincide");
    }

    @Test
    public void testSettersConValoresNulos() {
        // Establecer valores nulos en los campos que lo permiten
        repartidor.setIdUsuario(null);
        repartidor.setEmail(null);
        repartidor.setPass(null);
        repartidor.setNombre(null);
        repartidor.setApellidos(null);
        repartidor.setNif(null);
        repartidor.setEficiencia(null);

        // Verifica que los getters devuelven null donde corresponde
        assertNull(repartidor.getIdUsuario(), "El ID de usuario debería ser null");
        assertNull(repartidor.getEmail(), "El email debería ser null");
        assertNull(repartidor.getPass(), "La contraseña debería ser null");
        assertNull(repartidor.getNombre(), "El nombre debería ser null");
        assertNull(repartidor.getApellidos(), "Los apellidos deberían ser null");
        assertNull(repartidor.getNif(), "El NIF debería ser null");
        assertNull(repartidor.getEficiencia(), "La eficiencia debería ser null");
    }

    @Test
    public void testToString() {
        // Establecer valores para probar toString
        repartidor.setIdUsuario(1L);
        repartidor.setEmail("repartidor@email.com");
        repartidor.setPass("pass123");
        repartidor.setNombre("Ana");
        repartidor.setApellidos("Gómez");
        repartidor.setNif("87654321B");
        repartidor.setEficiencia(95);

        // Valor esperado de toString, incluyendo codigoPostal=null
        String expected = "Repartidor [idUsuario=1, pass=pass123, nombre=Ana, apellidos=Gómez, nif=87654321B, eficiencia=95, codigoPostal=null]";
        assertEquals(expected, repartidor.toString(), "El método toString no devuelve el formato esperado");
    }
}