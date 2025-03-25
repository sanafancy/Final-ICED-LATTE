package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.List;

@Controller
@RequestMapping("/cartas")
public class CartaMenuController {
    @Autowired
    private CartaMenuDAO cartaMenuDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @GetMapping("/crear")
    public String mostrarFormularioCreacion(Model model) {
        model.addAttribute("carta", new CartaMenu());
        return "crearCarta";
    }
    @PostMapping("/crear")
    public String crearCarta(@ModelAttribute CartaMenu carta, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");

        if (restaurante == null) {
            return "redirect:/login";
        }
        //carta.setId(null);
        carta.setRestaurante(restaurante);
        cartaMenuDAO.save(carta);
        return "redirect:/inicioRestaurante";
    }

    @GetMapping("/editar/{id}")
    public String editarCarta(@PathVariable Long id, Model model) {
        CartaMenu carta = cartaMenuDAO.findById(id).orElse(null);
        if (carta == null) {
            return "redirect:/cartas";
        }

        model.addAttribute("carta", carta);
        model.addAttribute("items", carta.getItems());
        model.addAttribute("itemNuevo", new ItemMenu());
        return "editarCarta";
    }
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

    @PostMapping("/eliminar/{id}")
    public String eliminarCarta(@PathVariable Long id) {
        cartaMenuDAO.deleteById(id);
        return "redirect:/inicioRestaurante";
    }

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

    @PostMapping("/editar/{cartaId}/eliminarItem/{itemId}")
    public String eliminarItem(@PathVariable Long cartaId, @PathVariable Long itemId) {
        itemMenuDAO.deleteById(itemId);
        return "redirect:/cartas/editar/" + cartaId;
    }
}
