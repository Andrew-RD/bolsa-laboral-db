package logico;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;

/**
 * Usuario compatible con la primera versión serializada. Los campos
 * {@code contrasena} y {@code tipo} se conservan exclusivamente para leer
 * objetos legados.
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    // Campos históricos: no renombrar ni cambiar su tipo.
    private String nombreUsuario;
    private String contrasena;
    private String tipo;

    private String identificador;
    private String nombreCompleto;
    private String correo;
    private RolUsuario rol;
    private Boolean activo;
    private EnumSet<Permiso> permisos;
    private LocalDate fechaCreacion;
    private byte[] passwordSalt;
    private byte[] passwordHash;
    private int passwordIteraciones;
    private String passwordAlgoritmo;

    /**
     * Firma histórica. Los objetos creados ahora protegen inmediatamente la
     * contraseña; solo los objetos realmente deserializados conservan texto
     * legado hasta autenticarse.
     */
    public Usuario(String nombreUsuario, String contrasena, String tipo) {
        this(nombreUsuario, nombreUsuario, "", parsearRol(tipo), true,
                contrasena == null ? null : contrasena.toCharArray());
    }

    public Usuario(String nombreCompleto, String nombreUsuario, String correo,
            RolUsuario rol, boolean activo, char[] password) {
        this.identificador = UUID.randomUUID().toString();
        this.nombreCompleto = limpiar(nombreCompleto);
        this.nombreUsuario = limpiar(nombreUsuario);
        this.correo = limpiar(correo);
        this.rol = rol;
        this.activo = Boolean.valueOf(activo);
        this.fechaCreacion = LocalDate.now();
        this.permisos = PermisosPorRol.predeterminados(rol);
        this.tipo = tipoLegado(rol);
        if (password != null) {
            establecerContrasena(password);
        }
    }

    /**
     * Inicializa campos ausentes al leer datos antiguos. Es idempotente y no
     * escribe ningún archivo.
     */
    public int migrarDatosDeserializados() {
        int cambios = 0;
        if (rol == null) {
            rol = parsearRol(tipo);
            cambios++;
        }
        String tipoEsperado = tipoLegado(rol);
        if (!tipoEsperado.equals(tipo)) {
            tipo = tipoEsperado;
            cambios++;
        }
        if (identificador == null || identificador.trim().isEmpty()) {
            String semilla = "usuario-legado:" + normalizar(nombreUsuario);
            identificador = UUID.nameUUIDFromBytes(
                    semilla.getBytes(StandardCharsets.UTF_8)).toString();
            cambios++;
        }
        if (nombreCompleto == null) {
            nombreCompleto = nombreUsuario == null ? "" : nombreUsuario.trim();
            cambios++;
        }
        if (correo == null) {
            correo = "";
            cambios++;
        }
        if (activo == null) {
            activo = Boolean.TRUE;
            cambios++;
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDate.now();
            cambios++;
        }
        if (permisos == null) {
            permisos = PermisosPorRol.predeterminados(rol);
            cambios++;
        } else if (rol == RolUsuario.ADMINISTRADOR
                && !permisos.containsAll(EnumSet.allOf(Permiso.class))) {
            permisos = EnumSet.allOf(Permiso.class);
            cambios++;
        }
        if (passwordHash != null && passwordHash.length > 0) {
            if (passwordIteraciones <= 0) {
                passwordIteraciones = PasswordService.ITERACIONES;
                cambios++;
            }
            if (passwordAlgoritmo == null || passwordAlgoritmo.trim().isEmpty()) {
                passwordAlgoritmo = PasswordService.ALGORITMO;
                cambios++;
            }
            if (contrasena != null) {
                contrasena = null;
                cambios++;
            }
        }
        return cambios;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = limpiar(nombreCompleto);
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = limpiar(nombreUsuario);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = limpiar(correo);
    }

    /**
     * Solo permite inspeccionar el valor legado durante compatibilidad. Para
     * usuarios nuevos siempre retorna {@code null}.
     */
    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String nuevaContrasena) {
        if (nuevaContrasena == null) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        char[] temporal = nuevaContrasena.toCharArray();
        try {
            establecerContrasena(temporal);
        } finally {
            Arrays.fill(temporal, '\0');
        }
    }

    public String getTipo() {
        return tipoLegado(getRol());
    }

    public void setTipo(String tipo) {
        setRol(parsearRol(tipo));
    }

    public RolUsuario getRol() {
        if (rol == null) {
            rol = parsearRol(tipo);
        }
        return rol;
    }

    public void setRol(RolUsuario rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }
        this.rol = rol;
        this.tipo = tipoLegado(rol);
        if (rol == RolUsuario.ADMINISTRADOR) {
            this.permisos = EnumSet.allOf(Permiso.class);
        } else if (this.permisos == null) {
            this.permisos = PermisosPorRol.predeterminados(rol);
        }
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public void setActivo(boolean activo) {
        this.activo = Boolean.valueOf(activo);
    }

    public EnumSet<Permiso> getPermisos() {
        if (permisos == null) {
            permisos = PermisosPorRol.predeterminados(getRol());
        }
        if (getRol() == RolUsuario.ADMINISTRADOR) {
            return EnumSet.allOf(Permiso.class);
        }
        return permisos.isEmpty() ? EnumSet.noneOf(Permiso.class) : EnumSet.copyOf(permisos);
    }

    public void setPermisos(Iterable<Permiso> nuevosPermisos) {
        if (getRol() == RolUsuario.ADMINISTRADOR) {
            permisos = EnumSet.allOf(Permiso.class);
            return;
        }
        EnumSet<Permiso> copia = EnumSet.noneOf(Permiso.class);
        if (nuevosPermisos != null) {
            for (Permiso permiso : nuevosPermisos) {
                if (permiso != null) {
                    copia.add(permiso);
                }
            }
        }
        permisos = copia;
    }

    public boolean tienePermiso(Permiso permiso) {
        return AutorizacionService.tienePermiso(this, permiso);
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public boolean tieneContrasenaProtegida() {
        return passwordSalt != null && passwordSalt.length > 0
                && passwordHash != null && passwordHash.length > 0
                && contrasena == null;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt == null ? null : passwordSalt.clone();
    }

    public byte[] getPasswordHash() {
        return passwordHash == null ? null : passwordHash.clone();
    }

    public void establecerContrasena(char[] password) {
        PasswordService.Credencial credencial = PasswordService.crear(password);
        passwordSalt = credencial.getSalt();
        passwordHash = credencial.getHash();
        passwordIteraciones = credencial.getIteraciones();
        passwordAlgoritmo = credencial.getAlgoritmo();
        contrasena = null;
    }

    /**
     * Autentica y migra una contraseña legada a PBKDF2 únicamente en memoria.
     */
    public boolean autenticar(char[] clave) {
        if (!isActivo() || clave == null) {
            return false;
        }
        if (passwordHash != null && passwordHash.length > 0) {
            return PasswordService.verificar(clave, passwordSalt, passwordHash,
                    passwordIteraciones, passwordAlgoritmo);
        }
        if (contrasena == null) {
            return false;
        }
        char[] legada = contrasena.toCharArray();
        boolean coincide;
        try {
            coincide = MessageDigestCompat.equals(legada, clave);
        } finally {
            Arrays.fill(legada, '\0');
        }
        if (coincide) {
            establecerContrasena(clave);
        }
        return coincide;
    }

    public boolean match(String nombre, String clave) {
        if (nombre == null || clave == null || nombreUsuario == null
                || !nombreUsuario.trim().equalsIgnoreCase(nombre.trim())) {
            return false;
        }
        char[] temporal = clave.toCharArray();
        try {
            return autenticar(temporal);
        } finally {
            Arrays.fill(temporal, '\0');
        }
    }

    private static RolUsuario parsearRol(String valor) {
        if (valor != null) {
            String limpio = valor.trim();
            if ("Admin".equalsIgnoreCase(limpio)
                    || "Administrador".equalsIgnoreCase(limpio)
                    || RolUsuario.ADMINISTRADOR.name().equalsIgnoreCase(limpio)) {
                return RolUsuario.ADMINISTRADOR;
            }
        }
        return RolUsuario.EMPLEADO;
    }

    private static String tipoLegado(RolUsuario rol) {
        return rol == RolUsuario.ADMINISTRADOR ? "Admin" : "Empleado";
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String normalizar(String valor) {
        return limpiar(valor).toLowerCase(Locale.ROOT);
    }

    /** Comparación constante para el único ciclo de vida restante del texto legado. */
    private static final class MessageDigestCompat {
        private MessageDigestCompat() {
        }

        private static boolean equals(char[] izquierda, char[] derecha) {
            int diferencia = izquierda.length ^ derecha.length;
            int longitud = Math.max(izquierda.length, derecha.length);
            for (int index = 0; index < longitud; index++) {
                char a = index < izquierda.length ? izquierda[index] : 0;
                char b = index < derecha.length ? derecha[index] : 0;
                diferencia |= a ^ b;
            }
            return diferencia == 0;
        }
    }
}
