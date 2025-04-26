package proyecto.iso2.dominio.entidades;

public enum CodigoPostal {
    CP_28001,
    CP_08001,
    CP_35001,
    CP_45600;

    @Override
    public String toString() {
        return name().substring(3); // Devuelve el código postal sin el prefijo "CP_"
    }
}
