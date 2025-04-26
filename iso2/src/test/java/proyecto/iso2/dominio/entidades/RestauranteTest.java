package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RestauranteTest {

    private Restaurante restaurante;
    private Direccion direccion;

    @BeforeEach
    public void setUp() {
        // Inicializamos una instancia de Direccion
        direccion = new Direccion("Calle Falsa", 123, "Piso 1", 28001, "Madrid");

        // Inicializamos una instancia de Restaurante para cada prueba
        restaurante = new Restaurante();
    }

    @Test
    public void testConstructorPorDefecto() {
        // Verifica que el constructor por defecto inicializa los campos correctamente
        assertNull(restaurante.getIdUsuario(), "El ID de usuario debería ser null");
        assertNull(restaurante.getEmail(), "El email debería ser null");
        assertNull(restaurante.getPass(), "La contraseña debería ser null");
        assertNull(restaurante.getNombre(), "El nombre debería ser null");
        assertNull(restaurante.getCif(), "El CIF debería ser null");
        assertNull(restaurante.getDireccion(), "La dirección debería ser null");
        assertNull(restaurante.getPedidos(), "La lista de pedidos debería ser null");
    }

    @Test
    public void testConstructorConParametros() {
        // Datos de prueba
        String email = "restaurante@email.com";
        String pass = "pass123";
        String nombre = "Pizzería Bella";
        String cif = "B12345678";

        // Crear una instancia usando el constructor con parámetros
        Restaurante nuevoRestaurante = new Restaurante(email, pass, nombre, cif, direccion);

        // Verifica que los campos se inicializan correctamente
        assertNull(nuevoRestaurante.getIdUsuario(), "El ID de usuario debería ser null");
        assertEquals(email, nuevoRestaurante.getEmail(), "El email no coincide");
        assertEquals(pass, nuevoRestaurante.getPass(), "La contraseña no coincide");
        assertEquals(nombre, nuevoRestaurante.getNombre(), "El nombre no coincide");
        assertEquals(cif, nuevoRestaurante.getCif(), "El CIF no coincide");
        assertEquals(direccion, nuevoRestaurante.getDireccion(), "La dirección no coincide");
        assertNull(nuevoRestaurante.getPedidos(), "La lista de pedidos debería ser null");
    }

    @Test
    public void testSettersAndGetters() {
        // Datos de prueba
        Long idUsuario = 1L;
        String email = "restaurante@email.com";
        String pass = "pass123";
        String nombre = "Café Central";
        String cif = "A87654321";
        List<Pedido> pedidos = new ArrayList<>();
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedidos.add(pedido);

        // Usar setters para establecer los valores
        restaurante.setIdUsuario(idUsuario);
        restaurante.setEmail(email);
        restaurante.setPass(pass);
        restaurante.setNombre(nombre);
        restaurante.setCif(cif);
        restaurante.setDireccion(direccion);
        restaurante.setPedidos(pedidos);

        // Verifica que los getters devuelven los valores correctos
        assertEquals(idUsuario, restaurante.getIdUsuario(), "El ID de usuario no coincide");
        assertEquals(email, restaurante.getEmail(), "El email no coincide");
        assertEquals(pass, restaurante.getPass(), "La contraseña no coincide");
        assertEquals(nombre, restaurante.getNombre(), "El nombre no coincide");
        assertEquals(cif, restaurante.getCif(), "El CIF no coincide");
        assertEquals(direccion, restaurante.getDireccion(), "La dirección no coincide");
        assertEquals(pedidos, restaurante.getPedidos(), "La lista de pedidos no coincide");
        assertEquals(1, restaurante.getPedidos().size(), "El tamaño de la lista de pedidos no coincide");
    }

    @Test
    public void testSettersConValoresNulos() {
        // Establecer valores nulos en los campos que lo permiten
        restaurante.setIdUsuario(null);
        restaurante.setEmail(null);
        restaurante.setPass(null);
        restaurante.setNombre(null);
        restaurante.setCif(null);
        restaurante.setDireccion(null);
        restaurante.setPedidos(null);

        // Verifica que los getters devuelven null donde corresponde
        assertNull(restaurante.getIdUsuario(), "El ID de usuario debería ser null");
        assertNull(restaurante.getEmail(), "El email debería ser null");
        assertNull(restaurante.getPass(), "La contraseña debería ser null");
        assertNull(restaurante.getNombre(), "El nombre debería ser null");
        assertNull(restaurante.getCif(), "El CIF debería ser null");
        assertNull(restaurante.getDireccion(), "La dirección debería ser null");
        assertNull(restaurante.getPedidos(), "La lista de pedidos debería ser null");
    }

    @Test
    public void testSetPedidosConListaNoVacia() {
        // Crear una lista de pedidos
        List<Pedido> pedidos = new ArrayList<>();
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        Pedido pedido2 = new Pedido();
        pedido2.setId(2L);
        pedidos.add(pedido1);
        pedidos.add(pedido2);

        // Asignar la lista y verificar
        restaurante.setPedidos(pedidos);
        assertNotNull(restaurante.getPedidos(), "La lista de pedidos no debería ser null");
        assertEquals(2, restaurante.getPedidos().size(), "El tamaño de la lista de pedidos no coincide");
        assertEquals(pedido1, restaurante.getPedidos().get(0), "El primer pedido no coincide");
        assertEquals(pedido2, restaurante.getPedidos().get(1), "El segundo pedido no coincide");
    }

    @Test
    public void testToString() {
        // Establecer valores para probar toString
        restaurante.setIdUsuario(1L);
        restaurante.setEmail("restaurante@email.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Pizzería Bella");
        restaurante.setCif("B12345678");
        restaurante.setDireccion(direccion);
        restaurante.setPedidos(new ArrayList<>());

        // Valor esperado de toString
        String expected = "Restaurante [idUsuario=1, pass=pass123, nombre=Pizzería Bella, cif=B12345678]";
        assertEquals(expected, restaurante.toString(), "El método toString no devuelve el formato esperado");
    }
}
