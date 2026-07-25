package logico;

import java.io.Serializable;

public enum SituacionAcademica implements Serializable {
    ESTUDIANTE("Estudiante"),
    EGRESADO("Egresado"),
    GRADUADO("Graduado"),
    NO_ESPECIFICADO("No especificado");

    private final String etiqueta;

    SituacionAcademica(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
