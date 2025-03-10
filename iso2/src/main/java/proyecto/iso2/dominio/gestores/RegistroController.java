package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.Cliente;
import proyecto.iso2.dominio.entidades.Restaurante;
import proyecto.iso2.dominio.entidades.Repartidor;
import proyecto.iso2.resistencia.ClienteDAO;
import proyecto.iso2.resistencia.RestauranteDAO;
import proyecto.iso2.resistencia.RepartidorDAO;

@RestController
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private ClienteDAO clienteDAO;

    @Autowired
    private RestauranteDAO restauranteDAO;

    @Autowired
    private RepartidorDAO repartidorDAO;

    @PostMapping("/cliente")
    public Cliente registrarCliente(@RequestBody Cliente cliente) {
        return clienteDAO.save(cliente);
    }

    @PostMapping("/restaurante")
    public Restaurante registrarRestaurante(@RequestBody Restaurante restaurante) {
        return restauranteDAO.save(restaurante);
    }

    @PostMapping("/repartidor")
    public Repartidor registrarRepartidor(@RequestBody Repartidor repartidor) {
        return repartidorDAO.save(repartidor);
    }
}