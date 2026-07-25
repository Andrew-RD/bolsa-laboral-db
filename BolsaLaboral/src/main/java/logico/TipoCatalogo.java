package logico;

import java.io.Serializable;

public enum TipoCatalogo implements Serializable {
    UNIVERSIDADES("Universidades"),
    CARRERAS("Carreras"),
    AREAS_TECNICAS("Áreas técnicas"),
    HABILIDADES("Habilidades"),
    IDIOMAS("Idiomas"),
    SECTORES_EMPRESARIALES("Sectores empresariales"),
    AREAS_LABORALES("Áreas laborales");

    private final String etiqueta;

    TipoCatalogo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
