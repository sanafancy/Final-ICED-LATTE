package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ServicioEntregaTest {

    private ServicioEntrega servicioEntrega;
    private Pedido pedido;
    private Direccion direccion;
    private Repartidor repartidor;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime fechaEntrega;

    @BeforeEach
    public void setUp() {
        // Inicializamos una instancia de Pedido
        pedido = new Pedido();
        pedido.setId(1L);

        // Inicializamos una instancia de Direccion
        direccion = new Direccion("Calle Falsa", 123, "Piso 1", 28001, "Madrid");
        direccion.setId(2L);

        // Inicializamos una instancia de Repartidor
        repartidor = new Repartidor("repartidor@email.com", "pass123", "Ana", "Gómez", "87654321B", 95);
        repartidor.setIdUsuario(3L);

        // Inicializamos fechas
        fechaRecepcion = LocalDateTime.of(2025, 4, 25, 15, 30);
        fechaEntrega = LocalDateTime.of(2025, 4, 25, 16, 0);

        // Inicializamos una instancia de ServicioEntrega para cada prueba
        servicioEntrega = new ServicioEntrega();
    }

    @Test
    public void testConstructorPorDefecto() {
        // Verifica que el constructor por defecto inicializa los campos correctamente
        assertNull(servicioEntrega.getId(), "El ID debería ser null");
        assertNull(servicioEntrega.getPedido(), "El pedido debería ser null");
        assertNull(servicioEntrega.getDireccion(), "La dirección debería ser null");
        assertNull(servicioEntrega.getRepartidor(), "El repartidor debería ser null");
        assertNull(servicioEntrega.getFechaRecepcion(), "La fecha de recepción debería ser null");
        assertNull(servicioEntrega.getFechaEntrega(), "La fecha de entrega debería ser null");
    }

    @Test
    public void testConstructorConParametros() {
        // Crear una instancia usando el constructor con parámetros
        ServicioEntrega nuevoServicio = new ServicioEntrega(1L, pedido, direccion, repartidor, fechaRecepcion, fechaEntrega);

        // Verifica que los campos se inicializan correctamente
        assertEquals(1L, nuevoServicio.getId(), "El ID no coincide");
        assertEquals(pedido, nuevoServicio.getPedido(), "El pedido no coincide");
        assertEquals(direccion, nuevoServicio.getDireccion(), "La dirección no coincide");
        assertEquals(repartidor, nuevoServicio.getRepartidor(), "El repartidor no coincide");
        assertEquals(fechaRecepcion, nuevoServicio.getFechaRecepcion(), "La fecha de recepción no coincide");
        assertEquals(fechaEntrega, nuevoServicio.getFechaEntrega(), "La fecha de entrega no coincide");
    }

    @Test
    public void testSettersAndGetters() {
        // Usar setters para establecer los valores
        servicioEntrega.setId(1L);
        servicioEntrega.setPedido(pedido);
        servicioEntrega.setDireccion(direccion);
        servicioEntrega.setRepartidor(repartidor);
        servicioEntrega.setFechaRecepcion(fechaRecepcion);
        servicioEntrega.setFechaEntrega(fechaEntrega);

        // Verifica que los getters devuelven los valores correctos
        assertEquals(1L, servicioEntrega.getId(), "El ID no coincide");
        assertEquals(pedido, servicioEntrega.getPedido(), "El pedido no coincide");
        assertEquals(direccion, servicioEntrega.getDireccion(), "La dirección no coincide");
        assertEquals(repartidor, servicioEntrega.getRepartidor(), "El repartidor no coincide");
        assertEquals(fechaRecepcion, servicioEntrega.getFechaRecepcion(), "La fecha de recepción no coincide");
        assertEquals(fechaEntrega, servicioEntrega.getFechaEntrega(), "La fecha de entrega no coincide");
    }

    @Test
    public void testSettersConValoresNulos() {
        // Establecer valores nulos en los campos que lo permiten
        servicioEntrega.setId(null);
        servicioEntrega.setPedido(null);
        servicioEntrega.setDireccion(null);
        servicioEntrega.setRepartidor(null);
        servicioEntrega.setFechaRecepcion(null);
        servicioEntrega.setFechaEntrega(null);

        // Verifica que los getters devuelven null donde corresponde
        assertNull(servicioEntrega.getId(), "El ID debería ser null");
        assertNull(servicioEntrega.getPedido(), "El pedido debería ser null");
        assertNull(servicioEntrega.getDireccion(), "La dirección debería ser null");
        assertNull(servicioEntrega.getRepartidor(), "El repartidor debería ser null");
        assertNull(servicioEntrega.getFechaRecepcion(), "La fecha de recepción debería ser null");
        assertNull(servicioEntrega.getFechaEntrega(), "La fecha de entrega debería ser null");
    }

}