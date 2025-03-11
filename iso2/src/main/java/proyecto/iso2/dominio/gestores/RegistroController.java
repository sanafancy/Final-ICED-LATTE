package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.resistencia.ClienteDAO;
import proyecto.iso2.resistencia.RestauranteDAO;
import proyecto.iso2.resistencia.RepartidorDAO;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private RepartidorDAO repartidorDAO;

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