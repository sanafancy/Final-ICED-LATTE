package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class PagoTest {

    private Pago pago;
    private Pedido pedido;
    private LocalDateTime fechaTransaccion;

    @BeforeEach
    public void setUp() {
        // Inicializamos una instancia de Pedido
        pedido = new Pedido();
        pedido.setId(1L);

        // Inicializamos una fecha de transacción
        fechaTransaccion = LocalDateTime.of(2025, 4, 25, 15, 30);

        // Inicializamos una instancia de Pago para cada prueba
        pago = new Pago();
    }

    @Test
    public void testConstructorPorDefecto() {
        // Verifica que el constructor por defecto inicializa los campos correctamente
        assertNull(pago.getIdTransaccion(), "El ID de transacción debería ser null");
        assertNull(pago.getMetodoPago(), "El método de pago debería ser null");
        assertNull(pago.getPedido(), "El pedido debería ser null");
        assertNull(pago.getFechaTransaccion(), "La fecha de transacción debería ser null");
    }

    @Test
    public void testConstructorConParametros() {
        // Datos de prueba
        MetodoPago metodoPago = MetodoPago.CREDIT_CARD;

        // Crear una instancia usando el constructor con parámetros
        Pago nuevoPago = new Pago(metodoPago, pedido, fechaTransaccion);

        // Verifica que los campos se inicializan correctamente
        assertNull(nuevoPago.getIdTransaccion(), "El ID de transacción debería ser null");
        assertEquals(metodoPago, nuevoPago.getMetodoPago(), "El método de pago no coincide");
        assertEquals(pedido, nuevoPago.getPedido(), "El pedido no coincide");
        assertEquals(fechaTransaccion, nuevoPago.getFechaTransaccion(), "La fecha de transacción no coincide");
    }

    @Test
    public void testSettersAndGetters() {
        // Datos de prueba
        Long idTransaccion = 1L;
        MetodoPago metodoPago = MetodoPago.PAYPAL;

        // Usar setters para establecer los valores
        pago.setIdTransaccion(idTransaccion);
        pago.setMetodoPago(metodoPago);
        pago.setPedido(pedido);
        pago.setFechaTransaccion(fechaTransaccion);

        // Verifica que los getters devuelven los valores correctos
        assertEquals(idTransaccion, pago.getIdTransaccion(), "El ID de transacción no coincide");
        assertEquals(metodoPago, pago.getMetodoPago(), "El método de pago no coincide");
        assertEquals(pedido, pago.getPedido(), "El pedido no coincide");
        assertEquals(fechaTransaccion, pago.getFechaTransaccion(), "La fecha de transacción no coincide");
    }

    @Test
    public void testSettersConValoresNulos() {
        // Establecer valores nulos en los campos que lo permiten
        pago.setIdTransaccion(null);
        pago.setMetodoPago(null);
        pago.setPedido(null);
        pago.setFechaTransaccion(null);

        // Verifica que los getters devuelven null donde corresponde
        assertNull(pago.getIdTransaccion(), "El ID de transacción debería ser null");
        assertNull(pago.getMetodoPago(), "El método de pago debería ser null");
        assertNull(pago.getPedido(), "El pedido debería ser null");
        assertNull(pago.getFechaTransaccion(), "La fecha de transacción debería ser null");
    }


}