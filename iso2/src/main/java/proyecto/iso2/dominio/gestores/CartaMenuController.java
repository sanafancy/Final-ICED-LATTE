package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cartas")
public class CartaMenuController {

    @Autowired
    private CartaMenuDAO cartaMenuDAO;

    @Autowired
    private ItemMenuDAO itemMenuDAO;

    // Mostrar formulario para crear una nueva carta
    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("carta", new CartaMenu());
        return "crearCarta";
    }

    // Procesar la creación de una nueva carta
    @PostMapping("/crear")
    public String crearCarta(@ModelAttribute CartaMenu carta, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");

        if (restaurante == null) {
            return "redirect:/login";
        }
        carta.setRestaurante(restaurante);
        cartaMenuDAO.save(carta);
        return "redirect:/inicioRestaurante";
    }



    // Mostrar formulario para editar una carta existente
    @GetMapping("/editar/{id}")
    public String editarCarta(@PathVariable Long id, Model model) {
        Optional<CartaMenu> opt = cartaMenuDAO.findById(id);
        if (opt.isEmpty()) return "redirect:/restaurante/panel";

        CartaMenu carta = opt.get();
        model.addAttribute("carta", carta);
        model.addAttribute("menus", cartaMenuDAO.findByCartaPadre(carta)); // menús hijos
        model.addAttribute("nuevoMenu", new CartaMenu()); // para el formulario
        return "editarCarta";
    }

    // Procesar actualización del nombre de la carta
    @PostMapping("/editar/{id}")
    public String actualizarCarta(@PathVariable Long id, @ModelAttribute CartaMenu cartaActualizada) {
        CartaMenu carta = cartaMenuDAO.findById(id).orElse(null);
        if (carta == null) {
            return "redirect:/inicioRestaurante";
        }

        carta.setNombre(cartaActualizada.getNombre());
        cartaMenuDAO.save(carta);
        return "redirect:/cartas/editar/" + id;
    }

    // Eliminar una carta
    @PostMapping("/eliminar/{id}")
    public String eliminarCarta(@PathVariable Long id) {
        cartaMenuDAO.deleteById(id);
        return "redirect:/inicioRestaurante";
    }

    // Agregar un nuevo item a la carta
    @PostMapping("/editar/{cartaId}/agregarItem")
    public String agregarItem(@PathVariable Long cartaId, @ModelAttribute ItemMenu item) {
        CartaMenu carta = cartaMenuDAO.findById(cartaId).orElse(null);
        if (carta == null) {
            return "redirect:/inicioRestaurante";
        }

        item.setCartaMenu(carta);
        itemMenuDAO.save(item);
        return "redirect:/cartas/editar/" + cartaId;
    }

    // Eliminar un item de la carta
    @PostMapping("/editar/{cartaId}/eliminarItem/{itemId}")
    public String eliminarItem(@PathVariable Long cartaId, @PathVariable Long itemId) {
        itemMenuDAO.deleteById(itemId);
        return "redirect:/cartas/editar/" + cartaId;
    }

    // Ver los items de una carta
    @GetMapping("/{id}")
    public String verItemsCarta(@PathVariable Long id, Model model) {
        CartaMenu carta = cartaMenuDAO.findById(id).orElse(null);
        if (carta == null) {
            return "redirect:/inicioRestaurante";
        }

        // Submenús
        List<CartaMenu> submenus = cartaMenuDAO.findByCartaPadre(carta);

        model.addAttribute("carta", carta);
        model.addAttribute("submenus", submenus);
        return "verCarta";
    }

    // Mostrar el formulario para agregar un nuevo menú (subcarta) a una carta
    @GetMapping("/{id}/añadirMenu")
    public String mostrarFormularioMenu(@PathVariable Long id, Model model) {
        Optional<CartaMenu> opt = cartaMenuDAO.findById(id);
        if (opt.isEmpty()) return "redirect:/restaurante/panel";

        model.addAttribute("cartaId", id); // id de la carta padre
        model.addAttribute("menu", new CartaMenu()); // nuevo menú (subcarta)
        return "añadirMenu"; // nombre del HTML
    }

    // Procesar la adición de un nuevo menú (subcarta)
    @PostMapping("/{id}/añadirMenu")
    public String añadirMenu(@PathVariable("id") Long cartaId, @ModelAttribute("menu") CartaMenu menu) {
        Optional<CartaMenu> cartaPadreOpt = cartaMenuDAO.findById(cartaId);
        if (cartaPadreOpt.isEmpty()) return "redirect:/restaurante/panel";

        CartaMenu cartaPadre = cartaPadreOpt.get();
        menu.setCartaPadre(cartaPadre);
        menu.setRestaurante(cartaPadre.getRestaurante());

        cartaMenuDAO.save(menu);

        return "redirect:/restaurante/panel";
    }
    @GetMapping("/menu/{id}")
    public String verMenu(@PathVariable Long id, Model model) {
        CartaMenu menu = cartaMenuDAO.findById(id).orElse(null);
        if (menu == null) return "redirect:/inicioRestaurante";
        List<ItemMenu> items = itemMenuDAO.findByCartaMenu(menu);
        model.addAttribute("menu", menu);
        model.addAttribute("items", items);
        model.addAttribute("itemNuevo", new ItemMenu());
        return "verMenu";
    }

    @PostMapping("/menu/{id}/agregarItem")
    public String agregarItemAMenu(@PathVariable Long id, @ModelAttribute ItemMenu item) {
        CartaMenu menu = cartaMenuDAO.findById(id).orElse(null);
        if (menu == null) return "redirect:/inicioRestaurante";

        item.setCartaMenu(menu);  // Asociar el ítem con el menú
        itemMenuDAO.save(item);
        return "redirect:/cartas/menu/" + id; // Redirigir al mismo menú
    }
    // Mostrar formulario de edición
    @GetMapping("/menu/{menuId}/editarItem/{itemId}")
    public String mostrarFormularioEdicionItem(@PathVariable Long menuId, @PathVariable Long itemId, Model model) {
        ItemMenu item = itemMenuDAO.findById(itemId).orElse(null);
        if (item == null) return "redirect:/cartas/menu/" + menuId;

        model.addAttribute("item", item);
        model.addAttribute("menuId", menuId);
        return "editarItem"; // HTML con el formulario
    }

    @PostMapping("/menu/{menuId}/editarItem/{itemId}")
    public String procesarEdicionItem(@PathVariable Long menuId, @PathVariable Long itemId, @ModelAttribute ItemMenu itemActualizado) {
        ItemMenu item = itemMenuDAO.findById(itemId).orElse(null);
        if (item == null) return "redirect:/cartas/menu/" + menuId;

        item.setNombre(itemActualizado.getNombre());
        item.setPrecio(itemActualizado.getPrecio());
        item.setTipo(itemActualizado.getTipo());
        itemMenuDAO.save(item);

        return "redirect:/cartas/menu/" + menuId;
    }
    @PostMapping("/menu/{menuId}/eliminarItem/{itemId}")
    public String eliminarItemMenu(@PathVariable Long menuId, @PathVariable Long itemId) {
        itemMenuDAO.deleteById(itemId);
        return "redirect:/cartas/menu/" + menuId;
    }
}
