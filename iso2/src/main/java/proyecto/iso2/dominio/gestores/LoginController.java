package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
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
        return "login";
    }
    @PostMapping
    public String login(@RequestParam String email, @RequestParam String pass, HttpSession sesion, Model model) {
        Optional<Usuario> usuarioOpt = usuarioDAO.findByEmailAndPass(email, pass);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get(); //consulta tipo de usuario segun email y pass
            if (usuario instanceof Cliente) {
                sesion.setAttribute("cliente", usuario); //guardar sesion como cliente
                return "redirect:/";
            } else if (usuario instanceof Restaurante) {
                sesion.setAttribute("restaurante", usuario);
                return "InicioRestaurante";
            } else if (usuario instanceof Repartidor) {
                sesion.setAttribute("repartidor", usuario);
                return "InicioRepartidor";
            }
        }
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession sesion) {
        sesion.invalidate(); // 🔹 Elimina la sesión
        return "redirect:/"; // Redirige a la página de inicio
    }
}
