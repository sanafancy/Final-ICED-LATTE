package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.CodigoPostal;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.persistencia.RepartidorDAO;

@Controller
@RequestMapping("/repartidor")
public class RepartidorController {

    @Autowired
    private RepartidorDAO repartidorDAO;

    @GetMapping("/seleccionar-codigo-postal")
    public String mostrarFormularioCodigoPostal(HttpSession session, Model model) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        model.addAttribute("codigosPostales", CodigoPostal.values());
        model.addAttribute("repartidor", repartidor);
        return "seleccionarCodigoPostal";
    }

    @PostMapping("/seleccionar-codigo-postal")
    public String guardarCodigoPostal(@RequestParam String codigoPostal, HttpSession session) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        try {
            int ordinal = Integer.parseInt(codigoPostal);
            CodigoPostal cp = CodigoPostal.values()[ordinal];
            repartidor.setCodigoPostal(cp);
            repartidorDAO.save(repartidor);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return "redirect:/repartidor/seleccionar-codigo-postal?error=invalid";
        }
        return "redirect:/repartidor/InicioRepartidor";
    }

    @GetMapping("/InicioRepartidor")
    public String mostrarInicioRepartidor(HttpSession session, Model model) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        model.addAttribute("repartidor", repartidor);
        return "InicioRepartidor";
    }
}