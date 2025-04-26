package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartaMenuTest {

    @Test
    public void testConstructorYGetters() {
        Restaurante restaurante = new Restaurante("correo@prueba.com", "pass123", "Restaurante Prueba", "CIF123", new Direccion());
        CartaMenu carta = new CartaMenu("Carta de Verano", restaurante);

        assertEquals("Carta de Verano", carta.getNombre());
        assertEquals(restaurante, carta.getRestaurante());
    }

    @Test
    public void testSetters() {
        Restaurante restaurante = new Restaurante("mail@mail.com", "1234", "Nombre", "CIF000", new Direccion());
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta de Invierno");
        carta.setRestaurante(restaurante);

        assertEquals("Carta de Invierno", carta.getNombre());
        assertEquals(restaurante, carta.getRestaurante());
    }

    @Test
    public void testItems() {
        CartaMenu carta = new CartaMenu();
        List<ItemMenu> items = new ArrayList<>();
        ItemMenu item1 = new ItemMenu();
        ItemMenu item2 = new ItemMenu();
        items.add(item1);
        items.add(item2);
        carta.setItems(items);

        assertEquals(2, carta.getItems().size());
    }

    @Test
    public void testToString() {
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Test");
        assertTrue(carta.toString().contains("Carta Test"));
    }
}

