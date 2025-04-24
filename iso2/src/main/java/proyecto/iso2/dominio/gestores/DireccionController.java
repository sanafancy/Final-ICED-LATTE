package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

@Controller
@RequestMapping("/direcciones")
public class DireccionController {
    @Autowired
    private DireccionDAO direccionDAO;
    @Autowired
    private RestauranteDAO restauranteDAO;

    @GetMapping
    public String mostrarDireccion(HttpSession session, Model model) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");

        if (restaurante == null) {
            return "redirect:/login";
        }

        // Verificamos si la dirección existe
        Direccion direccion = restaurante.getDireccion();
        if (direccion == null) {
            direccion = new Direccion(); // Creamos  direccion vacia para evitar errores
        }

        model.addAttribute("direccion", direccion);
        return "direcciones";
    }


    @PostMapping("/editar")
    public String editarDireccion(@ModelAttribute Direccion nuevaDireccion, HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");

        if (restaurante == null) {
            return "redirect:/login";
        }

        Direccion direccion = restaurante.getDireccion();
        if (direccion != null) {
            // Actualizar los datos de la dirección existente
            direccion.setCalle(nuevaDireccion.getCalle());
            direccion.setNumero(nuevaDireccion.getNumero());
            direccion.setComplemento(nuevaDireccion.getComplemento());
            direccion.setMunicipio(nuevaDireccion.getMunicipio());
            direccion.setCodigoPostal(nuevaDireccion.getCodigoPostal());

            direccionDAO.save(direccion);
        }

        return "redirect:/direcciones";
    }
}
