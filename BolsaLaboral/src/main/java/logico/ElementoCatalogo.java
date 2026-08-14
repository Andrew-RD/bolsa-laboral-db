package logico;

import java.io.Serializable;

public class ElementoCatalogo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String nombre;
    private Boolean activo;
    private String siglas;
    private String nombreCompleto;

    public ElementoCatalogo(String nombre) {
        this.nombre = limpiar(nombre);
        this.activo = Boolean.TRUE;
    }

    public static ElementoCatalogo universidad(String siglas, String nombreCompleto) {
        ElementoCatalogo universidad = new ElementoCatalogo(nombreCompleto);
        universidad.actualizarDatosUniversidad(siglas, nombreCompleto);
        return universidad;
    }

    public int migrarDatosDeserializados(TipoCatalogo tipo) {
        int cambios = 0;
        if (tipo == TipoCatalogo.UNIVERSIDADES) {
            if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
                nombreCompleto = nombre == null || nombre.trim().isEmpty()
                        ? "" : limpiar(nombre);
                cambios++;
            }
            if (siglas == null) {
                siglas = "";
                cambios++;
            }
            if (!nombreCompleto.equals(nombre)) {
                nombre = nombreCompleto;
                cambios++;
            }
        }
        if (activo == null) {
            activo = Boolean.TRUE;
            cambios++;
        }
        if (nombre == null) {
            nombre = "";
            cambios++;
        }
        return cambios;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSiglas() {
        return siglas == null ? "" : siglas;
    }

    public String getNombreCompleto() {
        return nombreCompleto == null || nombreCompleto.trim().isEmpty()
                ? nombre : nombreCompleto;
    }

    public String getNombreMostrado() {
        String corto = getSiglas();
        String completo = getNombreCompleto();
        return corto.isEmpty() ? completo : corto + " — " + completo;
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public void setActivo(boolean activo) {
        this.activo = Boolean.valueOf(activo);
    }

    void actualizarDatosUniversidad(String nuevasSiglas, String nuevoNombreCompleto) {
        String completo = limpiar(nuevoNombreCompleto);
        String corto = nuevasSiglas == null ? "" : nuevasSiglas.trim()
                .replaceAll("\\s+", " ");
        this.siglas = corto;
        this.nombreCompleto = completo;
        this.nombre = completo;
    }

    @Override
    public String toString() {
        return getNombreMostrado();
    }

    private static String limpiar(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del elemento es obligatorio.");
        }
        return valor.trim().replaceAll("\\s+", " ");
    }
}