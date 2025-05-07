package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

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
        return "registro";
    }

    @GetMapping("/cliente")
    public String showRegistroClientePage() {
        return "registroCliente";
    }

    @PostMapping("/cliente")
    public String registrarCliente(@RequestParam String email, @RequestParam String pass,
                                   @RequestParam String nombre, @RequestParam String apellidos,
                                   @RequestParam String dni) {
        Cliente cliente = new Cliente(email, pass, nombre, apellidos, dni);
        clienteDAO.save(cliente);
        return "redirect:/";
    }

    @GetMapping("/restaurante")
    public String showRegistroRestaurantePage() {
        return "registroRestaurante";
    }

    @PostMapping("/restaurante")
    public String registrarRestaurante(@RequestParam String email, @RequestParam String pass,
                                       @RequestParam String nombre, @RequestParam String cif) {
        Restaurante restaurante = new Restaurante(email, pass, nombre, cif, new Direccion());
        restauranteDAO.save(restaurante);
        return "redirect:/login";
    }

    @GetMapping("/repartidor")
    public String showRegistroRepartidorPage() {
        return "registroRepartidor";
    }

    @PostMapping("/repartidor")
    public String registrarRepartidor(@RequestParam String email, @RequestParam String pass,
                                      @RequestParam String nombre, @RequestParam String apellidos,
                                      @RequestParam String nif,
                                      HttpSession sesion) {

        Repartidor repartidor = new Repartidor(email, pass, nombre, apellidos, nif, 0);
        repartidorDAO.save(repartidor);

        // Guardar en sesión para mostrar datos en /inicioRepartidor
        sesion.setAttribute("repartidor", repartidor);

        return "redirect:/login";
    }



}