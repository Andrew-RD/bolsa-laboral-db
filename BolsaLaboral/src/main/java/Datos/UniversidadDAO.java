package Datos;

import logico.ElementoCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UniversidadDAO {

    public ArrayList<ElementoCatalogo> listarTodos() {
        ArrayList<ElementoCatalogo> resultado = new ArrayList<>();
        String sql = "SELECT id_universidad, siglas, nombre, activo FROM universidades";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ElementoCatalogo universidad = ElementoCatalogo.universidad(
                        rs.getString("siglas"), rs.getString("nombre"));
                universidad.setId(rs.getInt("id_universidad"));
                universidad.setActivo(rs.getBoolean("activo"));
                resultado.add(universidad);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo universidades desde la base de datos", e);
        }
        return resultado;
    }

    public void agregar(ElementoCatalogo universidad) {
        String sql = "INSERT INTO universidades (siglas, nombre, activo) VALUES (?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, universidad.getSiglas());
            ps.setString(2, universidad.getNombreCompleto());
            ps.setBoolean(3, universidad.isActivo());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    universidad.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando la universidad", e);
        }
    }

    public void modificar(ElementoCatalogo universidad) {
        if (universidad.getId() == null) {
            throw new IllegalStateException("La universidad no tiene id; primero debe agregarse.");
        }
        String sql = "UPDATE universidades SET siglas = ?, nombre = ? WHERE id_universidad = ?";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, universidad.getSiglas());
                ps.setString(2, universidad.getNombreCompleto());
                ps.setInt(3, universidad.getId());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe una universidad con id_universidad = "
                            + universidad.getId() + ".");
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error modificando la universidad", e);
        }
    }

    public void cambiarEstado(ElementoCatalogo universidad, boolean activo) {
        if (universidad.getId() == null) {
            throw new IllegalStateException("La universidad no tiene id; primero debe agregarse.");
        }
        String sql = "UPDATE universidades SET activo = ? WHERE id_universidad = ?";

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBoolean(1, activo);
                ps.setInt(2, universidad.getId());

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe una universidad con id_universidad = "
                            + universidad.getId() + ".");
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cambiando el estado de la universidad", e);
        }
    }
}