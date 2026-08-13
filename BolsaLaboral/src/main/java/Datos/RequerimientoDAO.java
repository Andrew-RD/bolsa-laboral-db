package Datos;

import logico.ElementoCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class RequerimientoDAO {

    private final String tablaSubtipo;

    public RequerimientoDAO(String tablaSubtipo) {
        this.tablaSubtipo = tablaSubtipo;
    }

    public ArrayList<ElementoCatalogo> listarTodos() {
        ArrayList<ElementoCatalogo> resultado = new ArrayList<>();
        String sql = "SELECT r.id_requerimiento, r.nombre, r.activo "
                + "FROM requerimientos r "
                + "JOIN " + tablaSubtipo + " s ON s.id_requerimiento = r.id_requerimiento";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ElementoCatalogo elemento = new ElementoCatalogo(rs.getString("nombre"));
                elemento.setId(rs.getInt("id_requerimiento"));
                elemento.setActivo(rs.getBoolean("activo"));
                resultado.add(elemento);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo " + tablaSubtipo + " desde la base de datos", e);
        }
        return resultado;
    }

    public void agregar(ElementoCatalogo elemento) {
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idGenerado;
                try (PreparedStatement psRequerimiento = con.prepareStatement(
                        "INSERT INTO requerimientos (nombre, activo) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    psRequerimiento.setString(1, elemento.getNombre());
                    psRequerimiento.setBoolean(2, elemento.isActivo());
                    psRequerimiento.executeUpdate();

                    try (ResultSet claves = psRequerimiento.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("No se obtuvo el id generado para requerimientos.");
                        }
                        idGenerado = claves.getInt(1);
                    }
                }

                try (PreparedStatement psSubtipo = con.prepareStatement(
                        "INSERT INTO " + tablaSubtipo + " (id_requerimiento) VALUES (?)")) {
                    psSubtipo.setInt(1, idGenerado);
                    psSubtipo.executeUpdate();
                }

                con.commit();
                elemento.setId(idGenerado);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando a " + tablaSubtipo, e);
        }
    }

    public void cambiarEstado(ElementoCatalogo elemento, boolean activo) {
        if (elemento.getId() == null) {
            throw new IllegalStateException("El elemento no tiene id; primero debe agregarse.");
        }
        String sql = "UPDATE requerimientos SET activo = ? WHERE id_requerimiento = ?";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activo);
                ps.setInt(2, elemento.getId());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe un requerimiento con id_requerimiento = "
                            + elemento.getId() + ".");
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cambiando el estado en requerimientos", e);
        }
    }
}