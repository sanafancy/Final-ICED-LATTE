package proyecto.iso2.dominio.entidades;

import jakarta.persistence.*;

@Entity
public class ItemMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String nombre;
    @Column
    private double precio;
    @Column
    private String tipo;
    @ManyToOne
    @JoinColumn(name = "carta_menu_id")
    private CartaMenu cartaMenu;
    //Constructor
    public ItemMenu(){}
    public ItemMenu(String nombre, double precio,String tipo, CartaMenu cartaMenu) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = tipo;
        this.cartaMenu = cartaMenu;
    }

    //Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre(){
        return nombre;
    }
    public double getPrecio() {
        return precio;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public CartaMenu getCartaMenu() {
        return cartaMenu;
    }
    public void setCartaMenu(CartaMenu cartaMenu) {
        this.cartaMenu = cartaMenu;
    }

    @Override
    public String toString() {
        return String.format("ItemMenu [id=%s, nombre=%s, precio=%.2f, tipo=%s]", id, nombre, precio, tipo);
    }
}