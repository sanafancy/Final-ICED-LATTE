package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;
import java.util.List;

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
    private ClienteDAO clienteDAO;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");

        if (cliente != null) {
            System.out.println("Cliente autenticado: " + cliente.getEmail());
            model.addAttribute("cliente", cliente);
        } else {
            System.out.println("No hay sesión iniciada");
        }

        List<Restaurante> restaurantes = restauranteDAO.findAll();
        model.addAttribute("restaurantes", restaurantes);

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
    @GetMapping("/carta")
    public String mostrarCarta(Model model, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        List<CartaMenu> menus = cartaMenuDAO.findByRestaurante(restaurante);
        model.addAttribute("menus", menus);
        return "carta";
    }

    @PostMapping("/añadirMenu")
    public String añadirMenu(@RequestParam String nombre, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        CartaMenu menu = new CartaMenu(nombre, restaurante);
        cartaMenuDAO.save(menu);
        return "redirect:/carta";
    }

    @PostMapping("/añadirItem")
    public String añadirItem(@RequestParam Long menuId, @RequestParam String nombre) {
        CartaMenu menu = cartaMenuDAO.findById(menuId).orElseThrow();
        ItemMenu item = new ItemMenu(nombre, menu);
        itemMenuDAO.save(item);
        return "redirect:/carta";
    }

    @GetMapping("/direcciones")
    public String mostrarDirecciones(Model model, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        List<Direccion> direcciones = direccionDAO.findByRestaurante(restaurante);
        model.addAttribute("direcciones", direcciones);
        return "direcciones";
    }

    @PostMapping("/añadirDireccion")
    public String añadirDireccion(@RequestParam String calle, @RequestParam String numero, @RequestParam String municipio, @RequestParam String codigoPostal, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        Direccion direccion = new Direccion(calle, numero,complemento, municipio, codigoPostal, restaurante);
        direccionDAO.save(direccion);
        return "redirect:/direcciones";
    }

    @PostMapping("/eliminarDireccion")
    public String eliminarDireccion(@RequestParam Long direccionId) {
        direccionDAO.deleteById(direccionId);
        return "redirect:/direcciones";
    }

    @GetMapping("/eliminarRestaurante")
    public String eliminarRestaurante(HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        restauranteDAO.delete(restaurante);
        session.invalidate();
        return "redirect:/";
    }
}
