package proyecto.iso2.dominio.entidades;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



    public class ItemMenuTest {

        @Test
        void testConstructorYGetters() {
            CartaMenu cartaMenuMock = mock(CartaMenu.class);
            ItemMenu item = new ItemMenu("Pizza", 10.99, "Principal", cartaMenuMock);

            assertEquals("Pizza", item.getNombre());
            assertEquals(10.99, item.getPrecio());
            assertEquals("Principal", item.getTipo());
            assertEquals(cartaMenuMock, item.getCartaMenu());
        }

        @Test
        void testSetters() {
            ItemMenu item = new ItemMenu();
            item.setNombre("Hamburguesa");
            item.setPrecio(8.5);
            item.setTipo("Bebida");

            assertEquals("Hamburguesa", item.getNombre());
            assertEquals(8.5, item.getPrecio());
            assertEquals("Bebida", item.getTipo());
        }

        /*@Test
        void testToString() {
            ItemMenu item = new ItemMenu("Ensalada", 5.5, "Entrante", null);
            item.setId(1L);
            String esperado = "ItemMenu [id=1, nombre=Ensalada, precio=5.50, tipo=Entrante]";
            assertEquals(esperado, item.toString());
        }*/

        @Test
        void testCartaMenuNull() {
            ItemMenu item = new ItemMenu("Tarta", 3.5, "Postre", null);
            assertNull(item.getCartaMenu());
        }
    }


