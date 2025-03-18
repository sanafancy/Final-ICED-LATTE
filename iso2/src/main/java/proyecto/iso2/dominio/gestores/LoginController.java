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

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class LoginController {
    @Autowired
    private UsuarioDAO usuarioDAO;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String pass, HttpSession sesion, Model model) {
        Optional<Usuario> usuarioOpt = usuarioDAO.findByEmailAndPass(email, pass);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get(); //consulta tipo de usuario segun email y pass
            sesion.setAttribute("usuario", usuario);
            if (usuario instanceof Cliente) {
                Cliente cliente = (Cliente) usuario;
                System.out.println("Cliente autenticado: " + cliente.getEmail());
                if (cliente.getFavoritos() == null) {
                    System.out.println("Favoritos es NULL. Inicializando...");
                    cliente.setFavoritos(new ArrayList<>());
                }
                System.out.println("Favoritos del cliente: " + cliente.getFavoritos());

                sesion.setAttribute("cliente", usuario); //guardar sesion como cliente
                //System.out.println("Sesión iniciada para Cliente: " + usuario.getEmail());
                return "redirect:/";
            } else if (usuario instanceof Restaurante) {
                sesion.setAttribute("restaurante", usuario);
                System.out.println("Sesión iniciada para Restaurante: " + usuario.getEmail());
                return "InicioRestaurante";
            } else if (usuario instanceof Repartidor) {
                sesion.setAttribute("repartidor", usuario);
                System.out.println("Sesión iniciada para Repartidor: " + usuario.getEmail());
                return "InicioRepartidor";
            }
        }
        System.out.println("Error: Usuario no encontrado o credenciales incorrectas");
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession sesion) {
        sesion.invalidate(); // 🔹 Elimina la sesión
        return "redirect:/"; // Redirige a la página de inicio
    }
}
