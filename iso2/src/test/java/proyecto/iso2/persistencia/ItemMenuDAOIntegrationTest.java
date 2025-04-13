package proyecto.iso2.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import proyecto.iso2.dominio.entidades.CartaMenu;
import proyecto.iso2.dominio.entidades.ItemMenu;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemMenuDAOIntegrationTest {

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Limpiamos la base de datos respetando las dependencias
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("DELETE FROM pedido_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM cliente_favoritos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM direccion_cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM ItemMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM CartaMenu").executeUpdate();
            entityManager.createQuery("DELETE FROM Pedido").executeUpdate();
            entityManager.createQuery("DELETE FROM Repartidor").executeUpdate();
            entityManager.createQuery("DELETE FROM Restaurante").executeUpdate();
            entityManager.createQuery("DELETE FROM Cliente").executeUpdate();
            entityManager.createQuery("DELETE FROM Direccion").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuario").executeUpdate();
            entityManager.flush();
            return null;
        });
    }

    @Test
    public void testCrudItemMenu() {
        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear una carta de menú
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Principal");
        carta.setRestaurante(restaurante);
        cartaMenuDAO.save(carta);
        entityManager.flush();

        // Crear un ítem de menú
        ItemMenu item = new ItemMenu();
        item.setNombre("Pizza");
        item.setPrecio(10.0);
        item.setTipo("Plato");
        item.setCartaMenu(carta);

        // Guardar el ítem
        itemMenuDAO.save(item);
        entityManager.flush();

        // Verificar que se ha guardado
        List<ItemMenu> items = itemMenuDAO.findAll();
        assertThat(items, hasSize(1));
        assertThat(items.get(0).getNombre(), is("Pizza"));
        assertThat(items.get(0).getPrecio(), is(10.0));
        assertThat(items.get(0).getCartaMenu().getNombre(), is("Carta Principal"));

        // Actualizar el ítem
        ItemMenu itemGuardado = items.get(0);
        itemGuardado.setNombre("Hamburguesa");
        itemMenuDAO.save(itemGuardado);
        entityManager.flush();

        // Verificar la actualización
        ItemMenu itemActualizado = itemMenuDAO.findById(itemGuardado.getId()).orElse(null);
        assertThat(itemActualizado, notNullValue());
        assertThat(itemActualizado.getNombre(), is("Hamburguesa"));

        // Eliminar el ítem
        itemMenuDAO.delete(itemActualizado);
        entityManager.flush();

        // Verificar la eliminación
        assertThat(itemMenuDAO.findAll(), hasSize(0));
    }

    @Test
    public void testFindByCartaMenu() {
        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear una carta de menú
        CartaMenu carta = new CartaMenu();
        carta.setNombre("Carta Principal");
        carta.setRestaurante(restaurante);
        cartaMenuDAO.save(carta);
        entityManager.flush();

        // Crear dos ítems para la carta
        ItemMenu item1 = new ItemMenu();
        item1.setNombre("Pizza");
        item1.setPrecio(10.0);
        item1.setTipo("Plato");
        item1.setCartaMenu(carta);
        itemMenuDAO.save(item1);

        ItemMenu item2 = new ItemMenu();
        item2.setNombre("Hamburguesa");
        item2.setPrecio(12.0);
        item2.setTipo("Plato");
        item2.setCartaMenu(carta);
        itemMenuDAO.save(item2);

        entityManager.flush();

        // Buscar ítems por carta
        List<ItemMenu> items = itemMenuDAO.findByCartaMenu(carta);
        assertThat(items, hasSize(2));
        assertThat(items, containsInAnyOrder(
                hasProperty("nombre", is("Pizza")),
                hasProperty("nombre", is("Hamburguesa"))
        ));
    }
}