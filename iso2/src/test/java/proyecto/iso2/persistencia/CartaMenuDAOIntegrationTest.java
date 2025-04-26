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
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CartaMenuDAOIntegrationTest {

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
    public void testCrudCartaMenu() {
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

        // Guardar la carta
        cartaMenuDAO.save(carta);
        entityManager.flush();

        // Verificar que se ha guardado
        List<CartaMenu> cartas = cartaMenuDAO.findAll();
        assertThat(cartas, hasSize(1));
        assertThat(cartas.get(0).getNombre(), is("Carta Principal"));
        assertThat(cartas.get(0).getRestaurante().getNombre(), is("Restaurante A"));

        // Actualizar la carta
        CartaMenu cartaGuardada = cartas.get(0);
        cartaGuardada.setNombre("Carta Actualizada");
        cartaMenuDAO.save(cartaGuardada);
        entityManager.flush();

        // Verificar la actualización
        CartaMenu cartaActualizada = cartaMenuDAO.findById(cartaGuardada.getId()).orElse(null);
        assertThat(cartaActualizada, notNullValue());
        assertThat(cartaActualizada.getNombre(), is("Carta Actualizada"));

        // Eliminar la carta
        cartaMenuDAO.delete(cartaActualizada);
        entityManager.flush();

        // Verificar la eliminación
        assertThat(cartaMenuDAO.findAll(), hasSize(0));
    }

    @Test
    public void testFindByRestaurante() {
        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Crear dos cartas para el restaurante
        CartaMenu carta1 = new CartaMenu();
        carta1.setNombre("Carta 1");
        carta1.setRestaurante(restaurante);
        cartaMenuDAO.save(carta1);

        CartaMenu carta2 = new CartaMenu();
        carta2.setNombre("Carta 2");
        carta2.setRestaurante(restaurante);
        cartaMenuDAO.save(carta2);

        entityManager.flush();

        // Buscar cartas por restaurante
        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
        assertThat(cartas, hasSize(2));
        assertThat(cartas, containsInAnyOrder(
                hasProperty("nombre", is("Carta 1")),
                hasProperty("nombre", is("Carta 2"))
        ));
    }
}