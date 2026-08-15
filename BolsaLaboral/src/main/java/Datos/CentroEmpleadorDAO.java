package Datos;

import logico.CentroEmpleador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CentroEmpleadorDAO {

    private static final String PREFIJO_CODIGO = "CEN-";

    private static final String SELECT_TODOS =
            "SELECT ce.id_centroEmpleador, ce.rnc, ce.nombre, " +
                    "ce.telefono, ce.correo, " +
                    "s.nombre AS sector, " +
                    "p.nombre AS provincia, " +
                    "m.nombre AS municipio " +
                    "FROM centrosEmpleadores ce " +
                    "JOIN sectores s ON s.id_sector = ce.id_sector " +
                    "JOIN municipio m ON m.id_municipio = ce.id_municipio " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "ORDER BY ce.nombre";

    private static final String SELECT_POR_RNC =
            "SELECT ce.id_centroEmpleador, ce.rnc, ce.nombre, " +
                    "ce.telefono, ce.correo, " +
                    "s.nombre AS sector, " +
                    "p.nombre AS provincia, " +
                    "m.nombre AS municipio " +
                    "FROM centrosEmpleadores ce " +
                    "JOIN sectores s ON s.id_sector = ce.id_sector " +
                    "JOIN municipio m ON m.id_municipio = ce.id_municipio " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "WHERE ce.rnc = ?";

    private static final String DELETE =
            "DELETE FROM centrosEmpleadores WHERE id_centroEmpleador = ?";

    private static final String INSERT =
            "INSERT INTO centrosEmpleadores " +
                    "(rnc, nombre, telefono, correo, id_sector, id_municipio) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE centrosEmpleadores SET " +
                    "rnc = ?, " +
                    "nombre = ?, " +
                    "telefono = ?, " +
                    "correo = ?, " +
                    "id_sector = ?, " +
                    "id_municipio = ? " +
                    "WHERE id_centroEmpleador = ?";

    private static final String SELECT_ID_SECTOR =
            "SELECT id_sector FROM sectores WHERE nombre = ?";

    private static final String SELECT_ID_MUNICIPIO =
            "SELECT m.id_municipio " +
                    "FROM municipio m " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "WHERE m.nombre = ? AND p.nombre = ?";

    public ArrayList<CentroEmpleador> listarTodos() {
        ArrayList<CentroEmpleador> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapearCentro(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error leyendo centros empleadores desde la base de datos",
                    e
            );
        }

        return resultado;
    }

    public CentroEmpleador buscarPorRnc(String rnc) {
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_RNC)) {

            ps.setString(1, rnc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearCentro(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error buscando el centro empleador por RNC",
                    e
            );
        }

        return null;
    }

    public void agregar(CentroEmpleador centro) {
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            int idSector = buscarIdSector(con, centro.getSector());

            int idMunicipio = buscarIdMunicipio(
                    con,
                    centro.getMunicipio(),
                    centro.getProvincia()
            );

            ps.setString(1, centro.getRnc());
            ps.setString(2, centro.getNombre());
            ps.setString(3, centro.getTelefono());
            ps.setString(4, centro.getCorreo());
            ps.setInt(5, idSector);
            ps.setInt(6, idMunicipio);

            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    int idGenerado = claves.getInt(1);
                    centro.setCodigo(crearCodigo(idGenerado));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error agregando el centro empleador",
                    e
            );
        }
    }

    public void modificar(CentroEmpleador centro) {
        int idCentro = extraerIdDelCodigo(centro.getCodigo());

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {

            int idSector = buscarIdSector(con, centro.getSector());

            int idMunicipio = buscarIdMunicipio(
                    con,
                    centro.getMunicipio(),
                    centro.getProvincia()
            );

            ps.setString(1, centro.getRnc());
            ps.setString(2, centro.getNombre());
            ps.setString(3, centro.getTelefono());
            ps.setString(4, centro.getCorreo());
            ps.setInt(5, idSector);
            ps.setInt(6, idMunicipio);
            ps.setInt(7, idCentro);

            int filasModificadas = ps.executeUpdate();

            if (filasModificadas == 0) {
                throw new SQLException(
                        "No existe un centro empleador con id = "
                                + idCentro
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error modificando el centro empleador",
                    e
            );
        }
    }

    public void eliminar(CentroEmpleador centro) {
        int idCentro = extraerIdDelCodigo(centro.getCodigo());

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(DELETE)) {
                ps.setInt(1, idCentro);

                int filasEliminadas = ps.executeUpdate();

                if (filasEliminadas == 0) {
                    throw new SQLException(
                            "No existe un centro empleador con id = "
                                    + idCentro
                    );
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error eliminando el centro empleador",
                    e
            );
        }
    }

    private int buscarIdSector(
            Connection con,
            String nombreSector) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_SECTOR)) {
            ps.setString(1, nombreSector);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_sector");
                }
            }
        }

        throw new SQLException(
                "No existe el sector '" + nombreSector + "'."
        );
    }

    private int buscarIdMunicipio(
            Connection con,
            String nombreMunicipio,
            String nombreProvincia) throws SQLException {

        try (PreparedStatement ps =
                     con.prepareStatement(SELECT_ID_MUNICIPIO)) {

            ps.setString(1, nombreMunicipio);
            ps.setString(2, nombreProvincia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_municipio");
                }
            }
        }

        throw new SQLException(
                "No existe el municipio '" + nombreMunicipio +
                        "' en la provincia '" + nombreProvincia + "'."
        );
    }

    private CentroEmpleador mapearCentro(ResultSet rs)
            throws SQLException {

        int idCentro = rs.getInt("id_centroEmpleador");

        return new CentroEmpleador(
                crearCodigo(idCentro),
                rs.getString("nombre"),
                rs.getString("sector"),
                rs.getString("provincia"),
                rs.getString("municipio"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("rnc")
        );
    }

    private String crearCodigo(int idCentro) {
        return PREFIJO_CODIGO + idCentro;
    }

    private int extraerIdDelCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalStateException(
                    "El centro empleador no tiene código."
            );
        }

        String valorNumerico = codigo.trim();

        if (valorNumerico.startsWith(PREFIJO_CODIGO)) {
            valorNumerico = valorNumerico.substring(
                    PREFIJO_CODIGO.length()
            );
        }

        try {
            return Integer.parseInt(valorNumerico);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Código de centro empleador inválido: " + codigo,
                    e
            );
        }
    }
}