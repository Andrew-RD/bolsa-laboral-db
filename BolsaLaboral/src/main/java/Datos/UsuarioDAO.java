package Datos;

import logico.Permiso;
import logico.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

public class UsuarioDAO {

    private static final String SELECT_TODOS =
            "SELECT u.id_usuario, u.nombreCompleto, u.correo, " +
                    "u.activo, u.fechaCreacion, u.nombreUsuario, " +
                    "u.contrasena, r.nombre AS tipo " + "FROM usuarios u " +
                    "JOIN roles r " +
                    "ON r.id_rol = u.id_rol";

    private static final String INSERT =
            "INSERT INTO usuarios " + "(nombreCompleto, correo, activo, nombreUsuario, " +
                    "contrasena, id_rol, fechaCreacion) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE usuarios SET " + "nombreCompleto = ?, " + "correo = ?, " + "activo = ?, " +
                    "nombreUsuario = ?, " + "contrasena = ?, " + "id_rol = ? " + "WHERE id_usuario = ?";

    private static final String SELECT_ID_ROL = "SELECT id_rol FROM roles WHERE nombre = ?";

    private static final String SELECT_PERMISOS_TODOS =
            "SELECT pu.id_usuario, p.etiqueta " +
                    "FROM permisos_usuarios pu " +
                    "JOIN permisos p ON p.id_permiso = pu.id_permiso " +
                    "WHERE pu.concedido = 1";

    private static final String SELECT_ID_PERMISO =
            "SELECT id_permiso FROM permisos WHERE etiqueta = ?";

    private static final String INSERT_PERMISO =
            "INSERT INTO permisos (etiqueta) VALUES (?)";

    private static final String DELETE_PERMISOS_USUARIO =
            "DELETE FROM permisos_usuarios WHERE id_usuario = ?";

    private static final String INSERT_PERMISO_USUARIO =
            "INSERT INTO permisos_usuarios (id_permiso, id_usuario, concedido) VALUES (?, ?, 1)";

    public ArrayList<Usuario> listarTodos() {
        ArrayList<Usuario> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            HashMap<Integer, EnumSet<Permiso>> permisosPorUsuario = cargarPermisosPorUsuario(con);

            while (rs.next()) {
                resultado.add(mapearUsuario(rs, permisosPorUsuario));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo usuarios desde la base de datos", e);
        }
        return resultado;
    }

    public void agregar(Usuario usuario) {
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    vincularDatosBasicos(con, ps, usuario);
                    ps.setDate(7, Date.valueOf(usuario.getFechaCreacion()));
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (claves.next()) {
                            usuario.setIdUsuario(claves.getInt(1));
                        }
                    }
                }

                guardarPermisos(con, usuario.getIdUsuario(), usuario.getPermisos());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando el usuario a la base de datos", e);
        }
    }

    public void modificar(Usuario usuario) {
        if (usuario.getIdUsuario() == null) {
            throw new IllegalStateException(
                    "El usuario no tiene id_usuario; primero debe agregarse con agregar().");
        }

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                    vincularDatosBasicos(con, ps, usuario);
                    ps.setInt(7, usuario.getIdUsuario());

                    int filas = ps.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("No existe un usuario con id_usuario = "
                                + usuario.getIdUsuario() + ".");
                    }
                }

                guardarPermisos(con, usuario.getIdUsuario(), usuario.getPermisos());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error modificando el usuario en la base de datos", e);
        }
    }

    private void guardarPermisos(Connection con, int idUsuario, Iterable<Permiso> permisos) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_PERMISOS_USUARIO)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
        if (permisos == null) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_PERMISO_USUARIO)) {
            for (Permiso permiso : permisos) {
                if (permiso == null) {
                    continue;
                }
                int idPermiso = buscarOCrearIdPermiso(con, permiso);
                ps.setInt(1, idPermiso);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
        }
    }

    private int buscarOCrearIdPermiso(Connection con, Permiso permiso) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_PERMISO)) {
            ps.setString(1, permiso.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_permiso");
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_PERMISO, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, permiso.name());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo crear el permiso '" + permiso.name() + "'.");
    }

    private HashMap<Integer, EnumSet<Permiso>> cargarPermisosPorUsuario(Connection con) throws SQLException {
        HashMap<Integer, EnumSet<Permiso>> resultado = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_PERMISOS_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                String etiqueta = rs.getString("etiqueta");

                Permiso permiso;
                try {
                    permiso = Permiso.valueOf(etiqueta);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                resultado.computeIfAbsent(idUsuario, k -> EnumSet.noneOf(Permiso.class)).add(permiso);
            }
        }
        return resultado;
    }

    private void vincularDatosBasicos(Connection con, PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getNombreCompleto());
        ps.setString(2, usuario.getCorreo());
        ps.setBoolean(3, usuario.isActivo());
        ps.setString(4, usuario.getNombreUsuario());
        ps.setString(5, usuario.getContrasena());
        ps.setInt(6, buscarIdRol(con, usuario.getTipo()));
    }

    private Usuario mapearUsuario(ResultSet rs, HashMap<Integer, EnumSet<Permiso>> permisosPorUsuario) throws SQLException {
        String nombreUsuario = rs.getString("nombreUsuario");
        String contrasena = rs.getString("contrasena");
        String tipo = rs.getString("tipo");

        if("Administrador".equalsIgnoreCase(tipo)) {
            tipo =  "Admin";
        }

        Usuario usuario = new Usuario(nombreUsuario, contrasena, tipo);
        int idUsuario = rs.getInt("id_usuario");
        usuario.setIdUsuario(idUsuario);
        usuario.setNombreCompleto(rs.getString("nombreCompleto"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setActivo(rs.getBoolean("activo"));

        Date fechaCreacion = rs.getDate("fechaCreacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDate());
        }
        EnumSet<Permiso> permisosGuardados = permisosPorUsuario.get(idUsuario);
        if (permisosGuardados != null) {
            usuario.setPermisos(permisosGuardados);
        }

        return usuario;
    }

    private int buscarIdRol(
            Connection con,
            String tipoUsuario) throws SQLException {

        String nombreRol = tipoUsuario;

        if ("Admin".equalsIgnoreCase(tipoUsuario)) {
            nombreRol = "Administrador";
        }

        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_ROL)) {
            ps.setString(1, nombreRol);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_rol");
                }
            }
        }

        throw new SQLException(
                "No existe el rol '" + nombreRol + "'.");
    }

}