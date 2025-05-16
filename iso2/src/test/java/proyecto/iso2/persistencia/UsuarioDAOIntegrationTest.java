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
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Usuario;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UsuarioDAOIntegrationTest {

    @Autowired
    private UsuarioDAO usuarioDAO;

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
    public void testCrudUsuario() {
        // Crear un usuario
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");

        // Guardar el usuario
        usuarioDAO.save(cliente);
        entityManager.flush();

        // Verificar que se ha guardado
        List<Usuario> usuarios = usuarioDAO.findAll();
        assertThat(usuarios, hasSize(1));
        assertThat(usuarios.get(0).getEmail(), is("cliente@ejemplo.com"));

        // Actualizar el usuario
        Usuario usuarioGuardado = usuarios.get(0);
        usuarioGuardado.setEmail("cliente_actualizado@ejemplo.com");
        usuarioDAO.save(usuarioGuardado);
        entityManager.flush();

        // Verificar la actualización
        Usuario usuarioActualizado = usuarioDAO.findById(usuarioGuardado.getIdUsuario()).orElse(null);
        assertThat(usuarioActualizado, notNullValue());
        assertThat(usuarioActualizado.getEmail(), is("cliente_actualizado@ejemplo.com"));

        // Eliminar el usuario
        usuarioDAO.delete(usuarioActualizado);
        entityManager.flush();

        // Verificar la eliminación
        assertThat(usuarioDAO.findAll(), hasSize(0));
    }

    @Test
    public void testFindByEmailAndPass() {
        // Crear un usuario
        Cliente cliente = new Cliente();
        cliente.setEmail("cliente@ejemplo.com");
        cliente.setPass("pass123");
        cliente.setNombre("Cliente A");
        cliente.setApellidos("Apellido A");
        cliente.setDni("12345678A");
        usuarioDAO.save(cliente);
        entityManager.flush();

        // Buscar por email y contraseña
        Optional<Usuario> usuario = usuarioDAO.findByEmailAndPass("cliente@ejemplo.com", "pass123");
        assertThat(usuario.isPresent(), is(true));
        assertThat(usuario.get().getEmail(), is("cliente@ejemplo.com"));

        // Buscar con credenciales incorrectas
        usuario = usuarioDAO.findByEmailAndPass("cliente@ejemplo.com", "pass456");
        assertThat(usuario.isPresent(), is(false));
    }
}