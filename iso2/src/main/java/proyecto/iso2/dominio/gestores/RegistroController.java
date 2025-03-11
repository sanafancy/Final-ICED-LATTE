package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.resistencia.*;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private RepartidorDAO repartidorDAO;

    @GetMapping
    public String showRegistroPage() {
        return "registro"; // Nombre de la vista (registro.html)
    }

    @GetMapping("/registroCliente")
    public String showRegistroClientePage() {
        return "registroCliente"; // Nombre de la vista (registroCliente.html)
    }

    @GetMapping("/registroRestaurante")
    public String showRegistroRestaurantePage() {
        return "registroRestaurante"; // Nombre de la vista (registroRestaurante.html)
    }

    @GetMapping("/registroRepartidor")
    public String showRegistroRepartidorPage() {
        return "registroRepartidor"; // Nombre de la vista (registroRepartidor.html)
    }

    @PostMapping("/registroCliente")
    public String registrarCliente(@RequestParam String nombre, @RequestParam String apellidos, @RequestParam String dni, @RequestParam String email, @RequestParam String pass) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        clienteDAO.save(cliente);
        return "redirect:/login";
    }

    @PostMapping("/registroRestaurante")
    public String registrarRestaurante(@RequestParam String nombre, @RequestParam String cif, @RequestParam String email, @RequestParam String pass) {
        Restaurante restaurante = new Restaurante(email, pass, nombre, cif);
        restauranteDAO.save(restaurante);
        return "redirect:/login";
    }

    @PostMapping("/registroRepartidor")
    public String registrarRepartidor(@RequestParam String nombre, @RequestParam String apellidos, @RequestParam String nif, @RequestParam String email, @RequestParam String pass) {
        Repartidor repartidor = new Repartidor(email, pass, nombre, apellidos, nif, 0.0);
        repartidorDAO.save(repartidor);
        return "redirect:/login";
    }
}