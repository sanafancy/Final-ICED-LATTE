package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.List;
import java.util.Optional;

@Controller
public class RestauranteController {
    @Autowired
    private RestauranteDAO restauranteDAO;
    @Autowired
    private CartaMenuDAO cartaMenuDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;
    @Autowired
    private DireccionDAO direccionDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

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

    //anadir menu, item y dirección
    @GetMapping("/inicioRestaurante")
    public String inicioRestaurante(HttpSession session, Model model) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");

        if (restaurante != null) {
            System.out.println("Restaurante autenticado: " + restaurante.getEmail());
            model.addAttribute("restaurante", restaurante);

            // listar cartas existentes
            List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
            model.addAttribute("cartas", cartas);
        } else {
            System.out.println("No hay sesión iniciada para restaurante");
            return "redirect:/login";
        }

        return "inicioRestaurante";
    }

    /*@GetMapping("/login")
    public String login() {
        return "login";
    }*/
        @PostMapping("/favorito/{id}")
        public String toggleFavorito (@PathVariable Long id, HttpSession session){
            Cliente cliente = (Cliente) session.getAttribute("cliente");
            if (cliente != null) {
                //Restaurante restaurante = restauranteDAO.findById(id).orElse(null);
                Optional<Restaurante> restauranteOpt = restauranteDAO.findById(id);
                if (restauranteOpt.isPresent()) {
                    Restaurante restaurante = restauranteOpt.get();
                    if (cliente.getFavoritos().contains(restaurante)) {
                        cliente.getFavoritos().remove(restaurante);
                    } else {
                        cliente.getFavoritos().add(restaurante);
                    }
                    usuarioDAO.save(cliente);
                }
            }
            return "redirect:/";
        }
        @GetMapping("/restaurantes/favoritos")
        public String verFavoritos (HttpSession session, Model model){
            Cliente cliente = (Cliente) session.getAttribute("cliente");

            if (cliente == null) {
                return "redirect:/login"; // Si no hay sesión, redirigir a login
            }

            model.addAttribute("favoritos", cliente.getFavoritos());
            return "favoritos"; // Página donde mostraremos la lista de favoritos

        }
}
