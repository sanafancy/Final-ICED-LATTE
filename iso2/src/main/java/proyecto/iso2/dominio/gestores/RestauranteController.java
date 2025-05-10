package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.*;

@Controller
@RequestMapping("/restaurante")
public class RestauranteController {

    @Autowired
    private RestauranteDAO restauranteDAO;
    @Autowired
    private CartaMenuDAO cartaMenuDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;
    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private DireccionDAO direccionDAO;
    /**
     * Home público: lista todos los restaurantes (cliente)
     */
    @GetMapping("/inicio")
    public String inicio(Model model, HttpSession session) {
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        model.addAttribute("restaurantes", restaurantes);

        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }

        return "inicio";
    }

    /**
     * Búsqueda de restaurantes
     */
    @GetMapping("/buscar")
    public String buscarRestaurante(@RequestParam(required = false) String busqueda,
                                    HttpSession session,
                                    Model model) {
        Set<Restaurante> resultados = new LinkedHashSet<>();

        if (busqueda != null && !busqueda.isBlank()) {
            resultados.addAll(restauranteDAO.findByNombreContainingIgnoreCase(busqueda));
            resultados.addAll(restauranteDAO.findByDireccion_CalleContainingIgnoreCase(busqueda));
            resultados.addAll(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase(busqueda));
            try {
                int cp = Integer.parseInt(busqueda);
                resultados.addAll(restauranteDAO.findByDireccion_CodigoPostal(cp));
            } catch (NumberFormatException ignored) {}
        } else {
            resultados.addAll(restauranteDAO.findAll());
        }

        model.addAttribute("restaurantes", List.copyOf(resultados));
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }
        return "inicio";
    }
    /**
     * Página principal del restaurante autenticado
     */
    @GetMapping("/panel")
    public String panel(HttpSession session, Model model) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        if (restaurante == null) return "redirect:/login";  // Redirigir al login si no hay restaurante en sesión

        // Agregar el nombre del restaurante al modelo
        model.addAttribute("restauranteNombre", restaurante.getNombre());  // Asumiendo que el restaurante tiene un campo "nombre"

        model.addAttribute("restaurante", restaurante);
        List<CartaMenu> cartas = cartaMenuDAO.findByRestauranteAndCartaPadreIsNull(restaurante);
        cartas.forEach(c -> c.setItems(itemMenuDAO.findByCartaMenu(c)));
        model.addAttribute("cartas", cartas);

        return "inicioRestaurante";  // Vista que muestra la información del restaurante
    }
    /**
     * Redirige desde /inicioRestaurante al panel (evita error 404)
     */
    @GetMapping("/inicioRestaurante")
    public String redirigirPanel() {
        return "redirect:/restaurante/panel";
    }
    /**
     * Alternar favorito (cliente)
     */
    @PostMapping("/favoritos/{id}")
    @ResponseBody
    public Map<String, Object> toggleFavorito(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Cliente cliente = (Cliente) session.getAttribute("cliente");

        if (cliente != null) {
            Optional<Restaurante> opt = restauranteDAO.findById(id);
            if (opt.isPresent()) {
                Restaurante r = opt.get();
                boolean added;
                if (cliente.getFavoritos().contains(r)) {
                    cliente.getFavoritos().remove(r);
                    response.put("status", "removed");
                    added = false;
                } else {
                    cliente.getFavoritos().add(r);
                    response.put("status", "added");
                    added = true;
                }
                usuarioDAO.save(cliente);
                response.put("nombre", r.getNombre());
                response.put("id", r.getIdUsuario());
            } else {
                response.put("status", "not_found");
            }
        } else {
            response.put("status", "unauthorized");
        }

        return response;
    }
    @GetMapping("/favoritos")
    public String verFavoritos(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) return "redirect:/login";

        model.addAttribute("favoritos", cliente.getFavoritos());
        return "favoritos";
    }
    @RequestMapping("/favoritos/{idRestaurante}")
    public String alternarFavorito(@PathVariable Long idRestaurante, HttpSession session, RedirectAttributes redirectAttributes) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        Restaurante restaurante = restauranteDAO.findById(idRestaurante).orElse(null);

        if (cliente != null && restaurante != null) {
            if (cliente.getFavoritos().contains(restaurante)) {
                cliente.getFavoritos().remove(restaurante);
                redirectAttributes.addFlashAttribute("mensaje", "Restaurante eliminado de favoritos");
            } else {
                cliente.getFavoritos().add(restaurante);
                redirectAttributes.addFlashAttribute("mensaje", "Restaurante añadido a favoritos");
            }

            usuarioDAO.save(cliente);  // Guarda cambios
            session.setAttribute("cliente", usuarioDAO.findById(cliente.getIdUsuario()).get());  // Refresca sesión
        }

        return "redirect:/favoritos";
    }
    @GetMapping("/crearCarta")
    public String formCrearCarta(Model model, HttpSession session) {
        if (session.getAttribute("restaurante")==null) return "redirect:/login";
        model.addAttribute("carta", new CartaMenu());
        return "crearCarta";
    }
    @PostMapping("/crearCarta")
    public String crearCarta(@ModelAttribute CartaMenu carta, HttpSession session) {
        Restaurante r = (Restaurante) session.getAttribute("restaurante");
        if (r == null) return "redirect:/login";

        carta.setRestaurante(r);
        cartaMenuDAO.save(carta);

        return "redirect:/restaurante/panel";
    }
    @GetMapping("/editarCarta/{id}")
    public String editarCarta(@PathVariable Long id, Model model, HttpSession session) {
        Restaurante r = (Restaurante) session.getAttribute("restaurante");
        if (r == null) return "redirect:/login";

        Optional<CartaMenu> optCarta = cartaMenuDAO.findById(id);
        if (optCarta.isEmpty()) return "redirect:/restaurante/panel";

        CartaMenu carta = optCarta.get();
        model.addAttribute("carta", carta);
        model.addAttribute("items", itemMenuDAO.findByCartaMenu(carta));
        return "editarCarta"; // Crea esta vista para editar nombre y añadir items
    }
    @PostMapping("/eliminarCarta/{id}")
    public String eliminarCarta(@PathVariable Long id, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        if (restaurante == null) return "redirect:/login";

        Optional<CartaMenu> optCarta = cartaMenuDAO.findById(id);
        optCarta.ifPresent(cartaMenuDAO::delete);

        return "redirect:/restaurante/panel";
    }

    @GetMapping("/carta/{id}")
    public String verCarta(@PathVariable Long id, Model model) {
        Optional<CartaMenu> opt = cartaMenuDAO.findById(id);
        if (opt.isEmpty()) return "redirect:/restaurante/panel";

        CartaMenu carta = opt.get();
        List<CartaMenu> submenus = cartaMenuDAO.findByCartaPadre(carta); // solo si los menús están estructurados como hijos
        model.addAttribute("carta", carta);
        model.addAttribute("submenus", submenus);

        return "verCarta";
    }

    @PostMapping("/restaurante/actualizarNombreCarta")
    public String actualizarNombreCarta(@RequestParam Long cartaId, @RequestParam String nuevoNombre) {
        Optional<CartaMenu> optCarta = cartaMenuDAO.findById(cartaId);
        if (optCarta.isPresent()) {
            CartaMenu carta = optCarta.get();
            carta.setNombre(nuevoNombre);
            cartaMenuDAO.save(carta);
        }
        return "redirect:/restaurante/editarCarta/" + cartaId;
    }

    @PostMapping("/anadirItem")
    public String anadirItem(@RequestParam Long cartaId,
                             @RequestParam String nombre,
                             @RequestParam String tipo,
                             @RequestParam double precio) {
        Optional<CartaMenu> optCarta = cartaMenuDAO.findById(cartaId);
        if (optCarta.isPresent()) {
            CartaMenu carta = optCarta.get();
            ItemMenu item = new ItemMenu();
            item.setNombre(nombre);
            item.setTipo(tipo);
            item.setPrecio(precio);
            item.setCartaMenu(carta); // <-- ASOCIACIÓN NECESARIA

            itemMenuDAO.save(item);   // <-- GUARDAR
        }
        return "redirect:/restaurante/editarCarta/" + cartaId;
    }

    /**
     * Eliminar restaurante autenticado
     */
    @PostMapping("/eliminar")
    public String eliminarRestaurante(HttpSession session) {
        Long restauranteId = (Long) session.getAttribute("restauranteId");

        if (restauranteId == null) {
            System.out.println(">>> No hay ID de restaurante en sesión");
            return "redirect:/login";
        }
        Optional<Restaurante> opt = restauranteDAO.findById(restauranteId);
        if (opt.isEmpty()) {
            System.out.println(">>> Restaurante no encontrado con ID: " + restauranteId);
            return "redirect:/login";
        }
        Restaurante restaurante = opt.get();
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Ver menús públicos de un restaurante
     */
    @GetMapping("/{id}")
    public String verMenuRestaurante(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Restaurante> opt = restauranteDAO.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/restaurante/home";
        }
        Restaurante restaurante = opt.get();
        model.addAttribute("restaurante", restaurante);

        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
        for (CartaMenu c : cartas) {
            c.setItems(itemMenuDAO.findByCartaMenu(c));
        }
        model.addAttribute("cartas", cartas);

        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("direcciones", cliente.getDirecciones());
            model.addAttribute("cliente", cliente);
        }
        return "verMenus";
    }

    @GetMapping("/editarDireccion")
    public String formEditarDireccion(Model model, HttpSession session) {
        Restaurante r = (Restaurante) session.getAttribute("restaurante");
        if (r==null) return "redirect:/login";
        model.addAttribute("direccion", direccionDAO.findById(r.getDireccion().getId()).orElse(new Direccion()));
        return "editarDireccion";
    }
    @PostMapping("/editarDireccion")
    public String editarDireccion(@ModelAttribute Direccion direc, HttpSession session) {
        Restaurante r = (Restaurante) session.getAttribute("restaurante");
        if (r==null) return "redirect:/login";
        Direccion saved = direccionDAO.save(direc);
        r.setDireccion(saved);
        restauranteDAO.save(r);
        return "redirect:/restaurante/panel";
    }

    @Controller
    @RequestMapping("/repartidor")
    public class RepartidorController {

        @GetMapping("/inicio")
        public String inicioRepartidor(HttpSession session, Model model) {
            Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");

            if (repartidor == null) {
                return "redirect:/login";  // Redirigir al login si no hay repartidor en sesión
            }
            model.addAttribute("repartidor", repartidor);
            return "inicioRepartidor";  // Vista que muestra los datos del repartidor
        }
    }



}