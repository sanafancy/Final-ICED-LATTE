package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.persistencia.RestauranteDAO;
import proyecto.iso2.dominio.entidades.Restaurante;
import java.util.List;

@Controller
public class RestauranteController {
    @Autowired
    private RestauranteDAO restauranteDAO;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        model.addAttribute("restaurantes", restaurantes);

        Cliente cliente = (Cliente) session.getAttribute("cliente"); //recuperar cliente de la sesion
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }
        return "inicio";
    }
    @GetMapping("/buscarRestaurante")
    public String buscarRestaurante(@RequestParam(required = false) String busqueda, Model model) {
        List<Restaurante> restaurantes;
        if (busqueda != null && !busqueda.isEmpty()) {
            restaurantes = restauranteDAO.findByNombreContainingIgnoreCase(busqueda);
            restaurantes.addAll(restauranteDAO.findByDireccion_CalleContainingIgnoreCase(busqueda));
            restaurantes.addAll(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase(busqueda));
            try {
                int codigoPostal = Integer.parseInt(busqueda);
                restaurantes.addAll(restauranteDAO.findByDireccion_CodigoPostal(codigoPostal));
            } catch (NumberFormatException e) {
                // No es un código postal válido, ignorar
            }
        } else {
            restaurantes = restauranteDAO.findAll();
        }
        model.addAttribute("restaurantes", restaurantes);
        return "inicio";
    }
    /*@GetMapping("/login")
    public String login() {
        return "login";
    }*/
}
