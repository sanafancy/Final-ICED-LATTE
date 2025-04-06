package proyecto.iso2.dominio.gestores;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RestauranteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void testContextLoads() {
        // Verifica que el contexto de Spring se cargue correctamente y que el controlador esté disponible
        assertNotNull(webApplicationContext.getBean(RestauranteController.class), "RestauranteController should be loaded in the application context");
    }
}

