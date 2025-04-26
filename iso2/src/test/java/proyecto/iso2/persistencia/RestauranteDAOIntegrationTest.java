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
import proyecto.iso2.dominio.entidades.Direccion;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RestauranteDAOIntegrationTest {

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private DireccionDAO direccionDAO;

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
    public void testCrudRestaurante() {
        // Crear una dirección
        Direccion direccion = new Direccion();
        direccion.setCalle("Calle Principal");
        direccion.setNumero(123);
        direccion.setComplemento("Piso 1");
        direccion.setMunicipio("Ciudad");
        direccion.setCodigoPostal(12345);

        // Persistir la dirección
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion);
            entityManager.flush();
            return null;
        });

        // Verificar que la dirección se ha guardado
        Direccion direccionGuardada = direccionDAO.findById(direccion.getId()).orElse(null);
        assertThat(direccionGuardada, notNullValue());
        assertThat(direccionGuardada.getCalle(), is("Calle Principal"));

        // Crear un restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setEmail("restaurante@ejemplo.com");
        restaurante.setPass("pass123");
        restaurante.setNombre("Restaurante A");
        restaurante.setCif("CIF123");
        restaurante.setDireccion(direccion);

        // Guardar el restaurante
        restauranteDAO.save(restaurante);
        entityManager.flush();

        // Verificar que se ha guardado
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getEmail(), is("restaurante@ejemplo.com"));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante A"));
        assertThat(restaurantes.get(0).getDireccion(), notNullValue());
        assertThat(restaurantes.get(0).getDireccion().getCalle(), is("Calle Principal"));

        // Actualizar el restaurante
        Restaurante restauranteGuardado = restaurantes.get(0);
        restauranteGuardado.setNombre("Restaurante B");
        restauranteDAO.save(restauranteGuardado);
        entityManager.flush();

        // Verificar la actualización
        Restaurante restauranteActualizado = restauranteDAO.findById(restauranteGuardado.getIdUsuario()).orElse(null);
        assertThat(restauranteActualizado, notNullValue());
        assertThat(restauranteActualizado.getNombre(), is("Restaurante B"));

        // Eliminar el restaurante
        restauranteDAO.delete(restauranteActualizado);
        entityManager.flush();

        // Verificar la eliminación
        assertThat(restauranteDAO.findAll(), hasSize(0));
    }

    @Test
    public void testFindByNombreContainingIgnoreCase() {
        // Crear una dirección para el primer restaurante
        Direccion direccion1 = new Direccion();
        direccion1.setCalle("Calle Principal");
        direccion1.setNumero(123);
        direccion1.setComplemento("Piso 1");
        direccion1.setMunicipio("Ciudad");
        direccion1.setCodigoPostal(12345);
        entityManager.persist(direccion1);
        entityManager.flush();

        // Crear el primer restaurante
        Restaurante restaurante1 = new Restaurante();
        restaurante1.setEmail("restaurante1@ejemplo.com");
        restaurante1.setPass("pass123");
        restaurante1.setNombre("Restaurante A");
        restaurante1.setCif("CIF123");
        restaurante1.setDireccion(direccion1);
        restauranteDAO.save(restaurante1);
        entityManager.flush();

        // Crear una dirección para el segundo restaurante
        Direccion direccion2 = new Direccion();
        direccion2.setCalle("Calle Secundaria");
        direccion2.setNumero(456);
        direccion2.setComplemento("Piso 2");
        direccion2.setMunicipio("Ciudad");
        direccion2.setCodigoPostal(67890);
        entityManager.persist(direccion2);
        entityManager.flush();

        // Crear el segundo restaurante
        Restaurante restaurante2 = new Restaurante();
        restaurante2.setEmail("restaurante2@ejemplo.com");
        restaurante2.setPass("pass123");
        restaurante2.setNombre("Restaurante ABC");
        restaurante2.setCif("CIF456");
        restaurante2.setDireccion(direccion2); // Usamos una dirección diferente
        restauranteDAO.save(restaurante2);
        entityManager.flush();

        // Buscar restaurantes por nombre (ignorando mayúsculas/minúsculas)
        List<Restaurante> restaurantes = restauranteDAO.findByNombreContainingIgnoreCase("restaurante a");
        assertThat(restaurantes, hasSize(2));
        assertThat(restaurantes, containsInAnyOrder(
                hasProperty("nombre", is("Restaurante A")),
                hasProperty("nombre", is("Restaurante ABC"))
        ));
    }

    @Test
    public void testFindByDireccion_CalleContainingIgnoreCase() {
        // Crear dos direcciones
        Direccion direccion1 = new Direccion();
        direccion1.setCalle("Calle Principal");
        direccion1.setNumero(123);
        direccion1.setComplemento("Piso 1");
        direccion1.setMunicipio("Ciudad");
        direccion1.setCodigoPostal(12345);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion1);
            entityManager.flush();
            return null;
        });

        Direccion direccion2 = new Direccion();
        direccion2.setCalle("Avenida Secundaria");
        direccion2.setNumero(456);
        direccion2.setComplemento("Piso 2");
        direccion2.setMunicipio("Ciudad");
        direccion2.setCodigoPostal(12345);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion2);
            entityManager.flush();
            return null;
        });

        // Crear dos restaurantes
        Restaurante restaurante1 = new Restaurante();
        restaurante1.setEmail("restaurante1@ejemplo.com");
        restaurante1.setPass("pass123");
        restaurante1.setNombre("Restaurante A");
        restaurante1.setCif("CIF123");
        restaurante1.setDireccion(direccion1);
        restauranteDAO.save(restaurante1);

        Restaurante restaurante2 = new Restaurante();
        restaurante2.setEmail("restaurante2@ejemplo.com");
        restaurante2.setPass("pass123");
        restaurante2.setNombre("Restaurante B");
        restaurante2.setCif("CIF456");
        restaurante2.setDireccion(direccion2);
        restauranteDAO.save(restaurante2);

        entityManager.flush();

        // Buscar por calle
        List<Restaurante> restaurantes = restauranteDAO.findByDireccion_CalleContainingIgnoreCase("calle");
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante A"));

        // Buscar con un valor parcial
        restaurantes = restauranteDAO.findByDireccion_CalleContainingIgnoreCase("avenida");
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante B"));
    }

    @Test
    public void testFindByDireccion_MunicipioContainingIgnoreCase() {
        // Crear dos direcciones
        Direccion direccion1 = new Direccion();
        direccion1.setCalle("Calle Principal");
        direccion1.setNumero(123);
        direccion1.setComplemento("Piso 1");
        direccion1.setMunicipio("Madrid");
        direccion1.setCodigoPostal(12345);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion1);
            entityManager.flush();
            return null;
        });

        Direccion direccion2 = new Direccion();
        direccion2.setCalle("Avenida Secundaria");
        direccion2.setNumero(456);
        direccion2.setComplemento("Piso 2");
        direccion2.setMunicipio("Barcelona");
        direccion2.setCodigoPostal(54321);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion2);
            entityManager.flush();
            return null;
        });

        // Crear dos restaurantes
        Restaurante restaurante1 = new Restaurante();
        restaurante1.setEmail("restaurante1@ejemplo.com");
        restaurante1.setPass("pass123");
        restaurante1.setNombre("Restaurante A");
        restaurante1.setCif("CIF123");
        restaurante1.setDireccion(direccion1);
        restauranteDAO.save(restaurante1);

        Restaurante restaurante2 = new Restaurante();
        restaurante2.setEmail("restaurante2@ejemplo.com");
        restaurante2.setPass("pass123");
        restaurante2.setNombre("Restaurante B");
        restaurante2.setCif("CIF456");
        restaurante2.setDireccion(direccion2);
        restauranteDAO.save(restaurante2);

        entityManager.flush();

        // Buscar por municipio
        List<Restaurante> restaurantes = restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("madrid");
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante A"));

        // Buscar con un valor parcial
        restaurantes = restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase("barcelona");
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante B"));
    }

    @Test
    public void testFindByDireccion_CodigoPostal() {
        // Crear dos direcciones
        Direccion direccion1 = new Direccion();
        direccion1.setCalle("Calle Principal");
        direccion1.setNumero(123);
        direccion1.setComplemento("Piso 1");
        direccion1.setMunicipio("Madrid");
        direccion1.setCodigoPostal(12345);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion1);
            entityManager.flush();
            return null;
        });

        Direccion direccion2 = new Direccion();
        direccion2.setCalle("Avenida Secundaria");
        direccion2.setNumero(456);
        direccion2.setComplemento("Piso 2");
        direccion2.setMunicipio("Barcelona");
        direccion2.setCodigoPostal(54321);
        transactionTemplate.execute(status -> {
            entityManager.persist(direccion2);
            entityManager.flush();
            return null;
        });

        // Crear dos restaurantes
        Restaurante restaurante1 = new Restaurante();
        restaurante1.setEmail("restaurante1@ejemplo.com");
        restaurante1.setPass("pass123");
        restaurante1.setNombre("Restaurante A");
        restaurante1.setCif("CIF123");
        restaurante1.setDireccion(direccion1);
        restauranteDAO.save(restaurante1);

        Restaurante restaurante2 = new Restaurante();
        restaurante2.setEmail("restaurante2@ejemplo.com");
        restaurante2.setPass("pass123");
        restaurante2.setNombre("Restaurante B");
        restaurante2.setCif("CIF456");
        restaurante2.setDireccion(direccion2);
        restauranteDAO.save(restaurante2);

        entityManager.flush();

        // Buscar por código postal
        List<Restaurante> restaurantes = restauranteDAO.findByDireccion_CodigoPostal(12345);
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante A"));

        restaurantes = restauranteDAO.findByDireccion_CodigoPostal(54321);
        assertThat(restaurantes, hasSize(1));
        assertThat(restaurantes.get(0).getNombre(), is("Restaurante B"));
    }
}