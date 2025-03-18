package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.persistencia.ClienteDAO;
import proyecto.iso2.persistencia.RestauranteDAO;
import proyecto.iso2.dominio.entidades.Restaurante;

import java.util.HashSet;
import java.util.List;

@Controller
public class RestauranteController {
    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private ClienteDAO clienteDAO;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        model.addAttribute("restaurantes", restaurantes);

        Cliente cliente = (Cliente) session.getAttribute("cliente");

        if (cliente != null) {
            System.out.println("Cliente autenticado: " + cliente.getEmail());
            model.addAttribute("cliente", cliente);
        } else {
            System.out.println("No hay sesión iniciada");
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
    @PostMapping("/favorito/{id}")
    public String toggleFavorito(@PathVariable Long id, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            Restaurante restaurante = restauranteDAO.findById(id).orElse(null);
            if (restaurante != null) {
                if (cliente.getFavoritos().contains(restaurante)) {
                    cliente.getFavoritos().remove(restaurante);
                } else {
                    cliente.getFavoritos().add(restaurante);
                }
                clienteDAO.save(cliente);  // Guardamos los cambios en la BD
                session.setAttribute("cliente", cliente); // Actualizamos la sesión
            }
        }
        return "redirect:/";
    }
    @GetMapping("/favoritos")
    public String verFavoritos(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("favoritos", cliente.getFavoritos());
        }
        return "favoritos";
    }
}
