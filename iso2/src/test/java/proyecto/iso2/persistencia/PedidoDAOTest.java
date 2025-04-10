package proyecto.iso2.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Pedido;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PedidoDAOTest {

    @Mock
    private PedidoDAO pedidoDAO;

    private Cliente cliente;
    private Restaurante restaurante;

    @BeforeEach
    public void setUp() {
        // Inicializar los mocks
        MockitoAnnotations.openMocks(this);

        // Crear un cliente y un restaurante para las pruebas
        Direccion direccion = new Direccion("Calle Falsa", 123, "", 28001, "Madrid");
        cliente = new Cliente("cliente@ejemplo.com", "pass123", "Cliente A", "12345678A", "2211");
        setField(cliente, "idUsuario", 1L);

        restaurante = new Restaurante("restaurante@ejemplo.com", "pass123", "Restaurante A", "CIF1", direccion);
        setField(restaurante, "idUsuario", 2L);
    }

    @Test
    public void testFindById_Exists() {
        // Datos de prueba
        Pedido pedido = new Pedido(cliente, restaurante);
        setField(pedido, "id", 1L);

        // Configurar el mock
        when(pedidoDAO.findById(1L)).thenReturn(Optional.of(pedido));

        // Ejecutar el método
        Optional<Pedido> result = pedidoDAO.findById(1L);

        // Verificar el resultado
        assertTrue(result.isPresent());
        assertEquals(cliente, result.get().getCliente());
        assertEquals(restaurante, result.get().getRestaurante());
    }

    @Test
    public void testFindById_NotExists() {
        // Configurar el mock
        when(pedidoDAO.findById(1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        Optional<Pedido> result = pedidoDAO.findById(1L);

        // Verificar el resultado
        assertFalse(result.isPresent());
    }

    @Test
    public void testSave() {
        // Datos de prueba
        Pedido pedido = new Pedido(cliente, restaurante);

        // Configurar el mock
        when(pedidoDAO.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido savedPedido = invocation.getArgument(0);
            setField(savedPedido, "id", 1L);
            return savedPedido;
        });

        // Ejecutar el método
        Pedido result = pedidoDAO.save(pedido);

        // Verificar el resultado
        assertNotNull(result.getId());
        assertEquals(1L, result.getId());
        assertEquals(cliente, result.getCliente());
        assertEquals(restaurante, result.getRestaurante());
    }

    @Test
    public void testFindAll() {
        // Datos de prueba
        Pedido pedido1 = new Pedido(cliente, restaurante);
        setField(pedido1, "id", 1L);
        Pedido pedido2 = new Pedido(cliente, restaurante);
        setField(pedido2, "id", 2L);
        List<Pedido> pedidos = Arrays.asList(pedido1, pedido2);

        // Configurar el mock
        when(pedidoDAO.findAll()).thenReturn(pedidos);

        // Ejecutar el método
        List<Pedido> result = pedidoDAO.findAll();

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals(cliente, result.get(0).getCliente());
        assertEquals(restaurante, result.get(0).getRestaurante());
    }

    @Test
    public void testDeleteById() {
        // Ejecutar el método
        pedidoDAO.deleteById(1L);

        // Verificar que se llamó al método deleteById
        verify(pedidoDAO, times(1)).deleteById(1L);
    }

    @Test
    public void testFindByCliente() {
        // Datos de prueba
        Pedido pedido1 = new Pedido(cliente, restaurante);
        setField(pedido1, "id", 1L);
        Pedido pedido2 = new Pedido(cliente, restaurante);
        setField(pedido2, "id", 2L);
        List<Pedido> pedidos = Arrays.asList(pedido1, pedido2);

        // Configurar el mock
        when(pedidoDAO.findByCliente(cliente)).thenReturn(pedidos);

        // Ejecutar el método
        List<Pedido> result = pedidoDAO.findByCliente(cliente);

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals(cliente, result.get(0).getCliente());
        assertEquals(cliente, result.get(1).getCliente());
    }

    @Test
    public void testFindByCliente_NoMatches() {
        // Crear un cliente diferente
        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        Cliente cliente2 = new Cliente("cliente2@ejemplo.com", "pass456", "Cliente B", "87654321B", "1122");
        setField(cliente2, "idUsuario", 3L);

        // Configurar el mock
        when(pedidoDAO.findByCliente(cliente2)).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Pedido> result = pedidoDAO.findByCliente(cliente2);

        // Verificar el resultado
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByRestaurante() {
        // Datos de prueba
        Pedido pedido1 = new Pedido(cliente, restaurante);
        setField(pedido1, "id", 1L);
        Pedido pedido2 = new Pedido(cliente, restaurante);
        setField(pedido2, "id", 2L);
        List<Pedido> pedidos = Arrays.asList(pedido1, pedido2);

        // Configurar el mock
        when(pedidoDAO.findByRestaurante(restaurante)).thenReturn(pedidos);

        // Ejecutar el método
        List<Pedido> result = pedidoDAO.findByRestaurante(restaurante);

        // Verificar el resultado
        assertEquals(2, result.size());
        assertEquals(restaurante, result.get(0).getRestaurante());
        assertEquals(restaurante, result.get(1).getRestaurante());
    }

    @Test
    public void testFindByRestaurante_NoMatches() {
        // Crear un restaurante diferente
        Direccion direccion2 = new Direccion("Calle Verdadera", 456, "", 28002, "Madrid Centro");
        Restaurante restaurante2 = new Restaurante("restaurante2@ejemplo.com", "pass456", "Restaurante B", "CIF2", direccion2);
        setField(restaurante2, "idUsuario", 3L);

        // Configurar el mock
        when(pedidoDAO.findByRestaurante(restaurante2)).thenReturn(Arrays.asList());

        // Ejecutar el método
        List<Pedido> result = pedidoDAO.findByRestaurante(restaurante2);

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