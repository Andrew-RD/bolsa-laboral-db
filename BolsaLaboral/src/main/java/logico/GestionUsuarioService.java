package logico;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.regex.Pattern;

/** Reglas de alta, modificación, estado y credenciales de usuarios. */
public final class GestionUsuarioService {

    private static final Pattern CORREO_VALIDO = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final BolsaLaboral bolsa;

    public GestionUsuarioService(BolsaLaboral bolsa) {
        if (bolsa == null) {
            throw new IllegalArgumentException("La bolsa laboral es obligatoria.");
        }
        this.bolsa = bolsa;
    }

    public Usuario registrar(String nombreCompleto, String nombreUsuario, String correo,
            char[] password, char[] confirmacion, RolUsuario rol, boolean activo,
            Iterable<Permiso> permisos) {
        exigirGestionUsuarios();
        validarDatos(nombreCompleto, nombreUsuario, correo, rol, null);
        validarContrasenas(password, confirmacion, true);

        Usuario usuario = new Usuario(nombreCompleto, nombreUsuario, correo, rol, activo, password);
        usuario.setPermisos(permisos == null ? PermisosPorRol.predeterminados(rol) : permisos);
        bolsa.getUsuarios().add(usuario);
        return usuario;
    }

    public void modificar(Usuario usuario, String nombreCompleto, String nombreUsuario,
            String correo, RolUsuario rol, boolean activo, Iterable<Permiso> permisos) {
        exigirGestionUsuarios();
        exigirUsuarioExistente(usuario);
        validarDatos(nombreCompleto, nombreUsuario, correo, rol, usuario);

        Usuario actor = bolsa.getUsuarioActual();
        if (actor == usuario && actor.getRol() == RolUsuario.ADMINISTRADOR && !activo) {
            throw new IllegalArgumentException("El administrador actual no puede desactivarse a sí mismo.");
        }

        EnumSet<Permiso> permisosFinales = copiarPermisos(permisos, rol);
        if (actor == usuario && actor.getRol() == RolUsuario.ADMINISTRADOR
                && !permisosFinales.contains(Permiso.GESTIONAR_USUARIOS)) {
            throw new IllegalArgumentException(
                    "El administrador actual no puede eliminar su permiso de gestión de usuarios.");
        }
        if (usuario.getRol() == RolUsuario.ADMINISTRADOR && usuario.isActivo()
                && (rol != RolUsuario.ADMINISTRADOR || !activo)
                && contarAdministradoresActivosExcepto(usuario) == 0) {
            throw new IllegalArgumentException(
                    "No se puede desactivar o degradar al último administrador activo.");
        }

        usuario.setNombreCompleto(nombreCompleto);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setActivo(activo);
        usuario.setPermisos(permisosFinales);
    }

    public void cambiarEstado(Usuario usuario, boolean activo) {
        modificar(usuario, usuario.getNombreCompleto(), usuario.getNombreUsuario(),
                usuario.getCorreo(), usuario.getRol(), activo, usuario.getPermisos());
    }

    public void restablecerContrasena(Usuario usuario, char[] password, char[] confirmacion) {
        exigirGestionUsuarios();
        exigirUsuarioExistente(usuario);
        validarContrasenas(password, confirmacion, true);
        usuario.establecerContrasena(password);
    }

    public void validarContrasenas(char[] password, char[] confirmacion, boolean obligatoria) {
        if (obligatoria && (password == null || password.length == 0)) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (!Arrays.equals(password, confirmacion)) {
            throw new IllegalArgumentException("La contraseña y su confirmación deben coincidir.");
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario, Usuario excluir) {
        String buscado = normalizar(nombreUsuario);
        for (Usuario usuario : bolsa.getUsuarios()) {
            if (usuario != null && usuario != excluir
                    && normalizar(usuario.getNombreUsuario()).equals(buscado)) {
                return true;
            }
        }
        return false;
    }

    public boolean existeCorreo(String correo, Usuario excluir) {
        String buscado = normalizar(correo);
        for (Usuario usuario : bolsa.getUsuarios()) {
            if (usuario != null && usuario != excluir
                    && normalizar(usuario.getCorreo()).equals(buscado)) {
                return true;
            }
        }
        return false;
    }

    public int contarAdministradoresActivos() {
        return contarAdministradoresActivosExcepto(null);
    }

    private void validarDatos(String nombreCompleto, String nombreUsuario, String correo,
            RolUsuario rol, Usuario excluir) {
        if (vacio(nombreCompleto)) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }
        if (vacio(nombreUsuario)) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (existeNombreUsuario(nombreUsuario, excluir)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese nombre, sin distinguir mayúsculas.");
        }
        if (vacio(correo) || !CORREO_VALIDO.matcher(correo.trim()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        if (existeCorreo(correo, excluir)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese correo, sin distinguir mayúsculas.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
    }

    private int contarAdministradoresActivosExcepto(Usuario excluir) {
        int total = 0;
        for (Usuario usuario : bolsa.getUsuarios()) {
            if (usuario != null && usuario != excluir && usuario.isActivo()
                    && usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                total++;
            }
        }
        return total;
    }

    private EnumSet<Permiso> copiarPermisos(Iterable<Permiso> permisos, RolUsuario rol) {
        if (rol == RolUsuario.ADMINISTRADOR) {
            return EnumSet.allOf(Permiso.class);
        }
        EnumSet<Permiso> copia = EnumSet.noneOf(Permiso.class);
        if (permisos != null) {
            for (Permiso permiso : permisos) {
                if (permiso != null) {
                    copia.add(permiso);
                }
            }
        }
        return copia;
    }

    private void exigirGestionUsuarios() {
        AutorizacionService.exigirPermiso(bolsa.getUsuarioActual(), Permiso.GESTIONAR_USUARIOS);
    }

    private void exigirUsuarioExistente(Usuario usuario) {
        if (usuario == null || !bolsa.getUsuarios().contains(usuario)) {
            throw new IllegalArgumentException("Debe seleccionar un usuario registrado.");
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }
}
