package logico;

import java.io.Serializable;

/**
 * Capacidades funcionales de la aplicación. Los nombres forman parte del
 * contrato serializado y no deben depender de textos visibles de la interfaz.
 */
public enum Permiso implements Serializable {
    CONSULTAR_CENTROS("Consultar centros"),
    GESTIONAR_CENTROS("Gestionar centros"),
    CONSULTAR_CANDIDATOS("Consultar candidatos"),
    GESTIONAR_CANDIDATOS("Gestionar candidatos"),
    CONSULTAR_OFERTAS("Consultar ofertas"),
    GESTIONAR_OFERTAS("Gestionar ofertas"),
    CONSULTAR_SOLICITUDES("Consultar solicitudes"),
    PROCESAR_SOLICITUDES("Procesar solicitudes"),
    USAR_PROCESAMIENTO_AVANZADO("Usar procesamiento avanzado"),
    VER_INFORMES("Ver informes administrativos"),
    GESTIONAR_RESPALDOS("Gestionar respaldos"),
    GESTIONAR_CATALOGOS("Gestionar catálogos"),
    GESTIONAR_USUARIOS("Gestionar usuarios");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
