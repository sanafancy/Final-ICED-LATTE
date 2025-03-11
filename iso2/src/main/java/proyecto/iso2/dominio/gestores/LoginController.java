package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.Usuario;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.persistencia.UsuarioDAO;
import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping
    public String showLoginPage() {
        return "login"; // Nombre de la vista (si usas Thymeleaf o similar)
    }
    @PostMapping
    public String login(@RequestParam String email, @RequestParam String pass) {
        Optional<Usuario> usuarioOpt = usuarioDAO.findByEmailAndPass(email, pass);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get(); //sistema consulta que tipo de usuario es en base a su email y pass
            if (usuario instanceof Cliente) {
                return "ListadoRestaurantes";
            } else if (usuario instanceof Restaurante) {
                return "InicioRestaurante";
            } else if (usuario instanceof Repartidor) {
                return "InicioRepartidor";
            }
        }
        return "redirect:/login?error=true";
    }

}
