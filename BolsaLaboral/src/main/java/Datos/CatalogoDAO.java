package Datos;

import logico.ElementoCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class CatalogoDAO {

    private final String tabla;
    private final String columnaId;

    public CatalogoDAO(String tabla, String columnaId) {
        this.tabla = tabla;
        this.columnaId = columnaId;
    }

    public ArrayList<ElementoCatalogo> listarTodos() {
        ArrayList<ElementoCatalogo> resultado = new ArrayList<>();
        String sql = "SELECT " + columnaId + ", nombre, activo FROM " + tabla;

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ElementoCatalogo elemento = new ElementoCatalogo(rs.getString("nombre"));
                elemento.setId(rs.getInt(columnaId));
                elemento.setActivo(rs.getBoolean("activo"));
                resultado.add(elemento);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo " + tabla + " desde la base de datos", e);
        }
        return resultado;
    }

    public void agregar(ElementoCatalogo elemento) {
        String sql = "INSERT INTO " + tabla + " (nombre, activo) VALUES (?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, elemento.getNombre());
            ps.setBoolean(2, elemento.isActivo());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    elemento.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando a " + tabla, e);
        }
    }

    public void cambiarEstado(ElementoCatalogo elemento, boolean activo) {
        if (elemento.getId() == null) {
            throw new IllegalStateException("El elemento no tiene id; primero debe agregarse.");
        }
        String sql = "UPDATE " + tabla + " SET activo = ? WHERE " + columnaId + " = ?";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activo);
                ps.setInt(2, elemento.getId());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe una fila en " + tabla
                            + " con " + columnaId + " = " + elemento.getId() + ".");
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cambiando el estado en " + tabla, e);
        }
    }
}