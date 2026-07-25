package logico;

import java.util.EnumSet;

/** Única fuente para los perfiles de permisos predeterminados. */
public final class PermisosPorRol {

    private PermisosPorRol() {
    }

    public static EnumSet<Permiso> predeterminados(RolUsuario rol) {
        if (RolUsuario.ADMINISTRADOR == rol) {
            return EnumSet.allOf(Permiso.class);
        }
        return EnumSet.of(
                Permiso.CONSULTAR_CENTROS,
                Permiso.GESTIONAR_CENTROS,
                Permiso.CONSULTAR_CANDIDATOS,
                Permiso.GESTIONAR_CANDIDATOS,
                Permiso.CONSULTAR_OFERTAS,
                Permiso.GESTIONAR_OFERTAS,
                Permiso.CONSULTAR_SOLICITUDES,
                Permiso.PROCESAR_SOLICITUDES);
    }
}
