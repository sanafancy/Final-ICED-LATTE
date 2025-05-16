package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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
    public String buscarRestaurante(@RequestParam(required = false) String busqueda,
                                    HttpSession session,
                                    Model model) {
        List<Restaurante> restaurantes;

        // Procesar la búsqueda
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

        // Agregar el cliente al modelo si está en sesión
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }

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

    @PostMapping("/favorito/{id}")
    public String toggleFavorito (@PathVariable Long id, HttpSession session){
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
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

    @GetMapping("/cliente/favoritos")
    public String verFavoritos(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/login";
        }

        cliente = (Cliente) usuarioDAO.findById(cliente.getIdUsuario()).orElse(null);
        if (cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("favoritos", cliente.getFavoritos());
        return "favoritos";  // Nombre de la plantilla favoritos.html
    }

    @PostMapping("/eliminarRestaurante")
    @Transactional //todas las operaciones seran exitosas o fallaran juntas
    public String eliminarRestaurante(HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        if (restaurante != null) {
            try{
                List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
                for (CartaMenu carta : cartas) { //borrar items de las cartas
                    itemMenuDAO.deleteByCartaMenu(carta);
                }
                cartaMenuDAO.deleteByRestaurante(restaurante); //borrar las cartas
                restauranteDAO.deleteById(restaurante.getIdUsuario()); //borrar el restaurante

                session.invalidate();
                return "redirect:/";
            }catch (Exception e){
                e.printStackTrace();
                return "redirect:/error";
            }
        }
        return "redirect:/"; // Redirige a la página de inicio
    }
    @GetMapping("/restaurante/{id}")
    public String verMenuRestaurante(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Restaurante> restauranteOpt = restauranteDAO.findById(id);
        if (restauranteOpt.isEmpty()) {
            return "redirect:/"; // Redirigir si el restaurante no existe
        }
        Restaurante restaurante = restauranteOpt.get();
        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);

        for (CartaMenu carta : cartas) {
            List<ItemMenu> items = itemMenuDAO.findByCartaMenu(carta);
            carta.setItems(items);
        }

        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("direcciones", cliente.getDirecciones());
        }

        model.addAttribute("restaurante", restaurante);
        model.addAttribute("cartas", cartas);
        model.addAttribute("cliente", cliente);
        //depurar:
        System.out.println("Cartas encontradas: " + cartas.size());
        for (CartaMenu carta : cartas) {
            System.out.println("Carta: " + carta.getNombre());
            List<ItemMenu> items = itemMenuDAO.findByCartaMenu(carta);
            System.out.println("Items en " + carta.getNombre() + ": " + items.size());
            for (ItemMenu item : items) {
                System.out.println(" - " + item.getNombre() + ": " + item.getPrecio() + "€");
            }
        }
        return "verMenus";
    }
}