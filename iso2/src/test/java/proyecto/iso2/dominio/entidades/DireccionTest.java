package proyecto.iso2.dominio.entidades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DireccionTest {

    private Direccion direccion;

    @BeforeEach
    public void setUp() {
        // Inicializamos una instancia de Direccion para cada prueba
        direccion = new Direccion();
    }

    @Test
    public void testConstructorPorDefecto() {
        // Verifica que el constructor por defecto inicializa los campos correctamente
        assertNull(direccion.getId(), "El ID debería ser null");
        assertNull(direccion.getCalle(), "La calle debería ser null");
        assertEquals(0, direccion.getNumero(), "El número debería ser 0");
        assertNull(direccion.getComplemento(), "El complemento debería ser null");
        assertEquals(0, direccion.getCodigoPostal(), "El código postal debería ser 0");
        assertNull(direccion.getMunicipio(), "El municipio debería ser null");
    }

    @Test
    public void testConstructorConParametros() {
        // Datos de prueba
        String calle = "Calle Falsa";
        int numero = 123;
        String complemento = "Piso 1";
        int codigoPostal = 28001;
        String municipio = "Madrid";

        // Crear una instancia usando el constructor con parámetros
        Direccion nuevaDireccion = new Direccion(calle, numero, complemento, codigoPostal, municipio);

        // Verifica que los campos se inicializan correctamente
        assertNull(nuevaDireccion.getId(), "El ID debería ser null");
        assertEquals(calle, nuevaDireccion.getCalle(), "La calle no coincide");
        assertEquals(numero, nuevaDireccion.getNumero(), "El número no coincide");
        assertEquals(complemento, nuevaDireccion.getComplemento(), "El complemento no coincide");
        assertEquals(codigoPostal, nuevaDireccion.getCodigoPostal(), "El código postal no coincide");
        assertEquals(municipio, nuevaDireccion.getMunicipio(), "El municipio no coincide");
    }

    @Test
    public void testSettersAndGetters() {
        // Datos de prueba
        Long id = 1L;
        String calle = "Avenida Siempre Viva";
        int numero = 742;
        String complemento = "Casa";
        int codigoPostal = 12345;
        String municipio = "Springfield";

        // Usar setters para establecer los valores
        direccion.setId(id);
        direccion.setCalle(calle);
        direccion.setNumero(numero);
        direccion.setComplemento(complemento);
        direccion.setCodigoPostal(codigoPostal);
        direccion.setMunicipio(municipio);

        // Verifica que los getters devuelven los valores correctos
        assertEquals(id, direccion.getId(), "El ID no coincide");
        assertEquals(calle, direccion.getCalle(), "La calle no coincide");
        assertEquals(numero, direccion.getNumero(), "El número no coincide");
        assertEquals(complemento, direccion.getComplemento(), "El complemento no coincide");
        assertEquals(codigoPostal, direccion.getCodigoPostal(), "El código postal no coincide");
        assertEquals(municipio, direccion.getMunicipio(), "El municipio no coincide");
    }

    @Test
    public void testSettersConValoresNulos() {
        // Establecer valores nulos en los campos que lo permiten
        direccion.setId(null);
        direccion.setCalle(null);
        direccion.setComplemento(null);
        direccion.setMunicipio(null);

        // Verifica que los getters devuelven null donde corresponde
        assertNull(direccion.getId(), "El ID debería ser null");
        assertNull(direccion.getCalle(), "La calle debería ser null");
        assertNull(direccion.getComplemento(), "El complemento debería ser null");
        assertNull(direccion.getMunicipio(), "El municipio debería ser null");

        // Los campos int (numero y codigoPostal) no pueden ser null, se inicializan en 0
        assertEquals(0, direccion.getNumero(), "El número debería ser 0");
        assertEquals(0, direccion.getCodigoPostal(), "El código postal debería ser 0");
    }

}