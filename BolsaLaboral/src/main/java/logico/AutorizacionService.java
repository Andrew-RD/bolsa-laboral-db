package logico;

import exception.AutorizacionException;

/** Punto central para consultar y exigir permisos. */
public final class AutorizacionService {

    private AutorizacionService() {
    }

    public static boolean tienePermiso(Usuario usuario, Permiso permiso) {
        if (usuario == null || permiso == null || !usuario.isActivo()) {
            return false;
        }
        if (RolUsuario.ADMINISTRADOR == usuario.getRol()) {
            return true;
        }
        return usuario.getPermisos().contains(permiso);
    }

    public static void exigirPermiso(Usuario usuario, Permiso permiso) {
        if (!tienePermiso(usuario, permiso)) {
            throw new AutorizacionException(permiso);
        }
    }
}
