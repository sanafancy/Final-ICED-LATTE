package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.persistencia.RestauranteDAO;

import java.util.List;

@Controller
@RequestMapping("/inicio")
public class InicioController {

    @Autowired
    private RestauranteDAO restauranteDAO;

    @GetMapping
    public String showInicioPage(Model model) {
        System.out.println("Se está llamando a /inicio");

        List<Restaurante> restaurantes = restauranteDAO.findAll(); // obtenemos todos
        model.addAttribute("restaurantes", restaurantes); // los pasamos a la vista

        return "inicio"; // renderiza inicio.html
    }
}
