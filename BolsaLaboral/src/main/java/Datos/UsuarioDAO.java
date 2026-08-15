package Datos;

import logico.Usuario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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

    public ArrayList<Usuario> listarTodos() {
        ArrayList<Usuario> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo usuarios desde la base de datos", e);
        }
        return resultado;
    }

    public void agregar(Usuario usuario) {
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            vincularDatosBasicos(con, ps, usuario);
            ps.setDate(7, Date.valueOf(usuario.getFechaCreacion()));
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setIdUsuario(claves.getInt(1));
                }
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
            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                vincularDatosBasicos(con, ps, usuario);
                ps.setInt(7, usuario.getIdUsuario());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe un usuario con id_usuario = "
                            + usuario.getIdUsuario() + ".");
                }
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

    private void vincularDatosBasicos(Connection con, PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getNombreCompleto());
        ps.setString(2, usuario.getCorreo());
        ps.setBoolean(3, usuario.isActivo());
        ps.setString(4, usuario.getNombreUsuario());
        ps.setString(5, usuario.getContrasena());
        ps.setInt(6, buscarIdRol(con, usuario.getTipo()));
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        String nombreUsuario = rs.getString("nombreUsuario");
        String contrasena = rs.getString("contrasena");
        String tipo = rs.getString("tipo");

        if("Administrador".equalsIgnoreCase(tipo)) {
            tipo =  "Admin";
        }

        Usuario usuario = new Usuario(nombreUsuario, contrasena, tipo);
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombreCompleto(rs.getString("nombreCompleto"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setActivo(rs.getBoolean("activo"));

        Date fechaCreacion = rs.getDate("fechaCreacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDate());
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