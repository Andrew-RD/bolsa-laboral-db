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
            "SELECT id_usuario, nombreCompleto, correo, activo, fechaCreacion, nombre_usuario, contrasena, tipo " +
                    "FROM usuarios";

    private static final String INSERT =
            "INSERT INTO usuarios (nombreCompleto, correo, activo, nombre_usuario, contrasena, tipo, fechaCreacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE usuarios SET nombreCompleto = ?, correo = ?, activo = ?, nombre_usuario = ?, " +
                    "contrasena = ?, tipo = ? WHERE id_usuario = ?";

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

            vincularDatosBasicos(ps, usuario);
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
                vincularDatosBasicos(ps, usuario);
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

    private void vincularDatosBasicos(PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getNombreCompleto());
        ps.setString(2, usuario.getCorreo());
        ps.setBoolean(3, usuario.isActivo());
        ps.setString(4, usuario.getNombreUsuario());
        ps.setString(5, usuario.getContrasena());
        ps.setString(6, usuario.getTipo());
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        String nombreUsuario = rs.getString("nombre_usuario");
        String contrasena = rs.getString("contrasena");
        String tipo = rs.getString("tipo");

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
}