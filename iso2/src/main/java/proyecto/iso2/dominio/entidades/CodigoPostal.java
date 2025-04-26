package proyecto.iso2.dominio.entidades;

public enum CodigoPostal {
    MADRID("28001"),
    BARCELONA("08001"),
    VALENCIA("46001"),
    SEVILLA("41001"),
    ZARAGOZA("50001"),
    MALAGA("29001"),
    MURCIA("30001"),
    PALMA_DE_MALLORCA("07001"),
    LAS_PALMAS_DE_GRAN_CANARIA("35001"),
    BILBAO("48001"),
    ALICANTE("03001"),
    TALAVERA_DE_LA_REINA("45600");

    private final String codigo;

    CodigoPostal(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
