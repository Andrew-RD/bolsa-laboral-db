package logico;

import java.io.Serializable;

/** Roles internos estables, independientes de las etiquetas históricas. */
public enum RolUsuario implements Serializable {
    ADMINISTRADOR("Administrador"),
    EMPLEADO("Empleado");

    private final String descripcion;

    RolUsuario(String descripcion) {
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
