package proyecto.iso2.dominio.entidades;

public enum CodigoPostal {
    CP_12300,
    CP_23400,
    CP_45600,
    CP_56700;

    @Override
    public String toString() {
        return name().substring(3); // Devuelve el código postal sin el prefijo "CP_"
    }
}
