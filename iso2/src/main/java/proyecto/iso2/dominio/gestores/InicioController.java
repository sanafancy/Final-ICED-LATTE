package proyecto.iso2.dominio.gestores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inicio")
public class InicioController {

    @GetMapping
    public String showInicioPage() {
        return "inicio"; // Nombre de la vista (inicio.html)
    }
}