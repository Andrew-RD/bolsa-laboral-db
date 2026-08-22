package Datos;

import logico.BrechaOfertaDemandaDTO;
import logico.Candidato;
import logico.ManoObraMunicipioDTO;
import logico.OfertaLaboral;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Consultas gerenciales exclusivamente de lectura. */
public class DashboardDAO {

    private static final String SELECT_BRECHA_POR_AREA =
            "SELECT al.nombre AS area, " +
                    "COUNT(DISTINCT o.id_oferta) AS ofertas_activas, " +
                    "COUNT(DISTINCT c.id_candidato) AS candidatos_desempleados " +
                    "FROM areasLaborales al " +
                    "LEFT JOIN ofertas o ON o.id_areaLaboral = al.id_areaLaboral " +
                    "AND o.estado = ? " +
                    "LEFT JOIN candidatos c ON c.id_areaLaboral = al.id_areaLaboral " +
                    "AND c.estado = ? " +
                    "GROUP BY al.id_areaLaboral, al.nombre " +
                    "ORDER BY ofertas_activas DESC, al.nombre";

    private static final String SELECT_MANO_OBRA_POR_MUNICIPIO =
            "WITH candidatos_por_municipio AS ( " +
                    "SELECT id_municipio, COUNT(*) AS candidatos_desempleados " +
                    "FROM candidatos " +
                    "WHERE estado = ? " +
                    "GROUP BY id_municipio" +
                    "), " +
                    "vacantes_por_municipio AS ( " +
                    "SELECT ce.id_municipio, SUM(o.vacantesTotales) AS vacantes_disponibles " +
                    "FROM centrosEmpleadores ce " +
                    "JOIN ofertas o ON o.id_centroEmpleador = ce.id_centroEmpleador " +
                    "WHERE o.estado = ? " +
                    "GROUP BY ce.id_municipio" +
                    ") " +
                    "SELECT p.nombre AS provincia, m.nombre AS municipio, " +
                    "COALESCE(cp.candidatos_desempleados, 0) AS candidatos_desempleados, " +
                    "COALESCE(vp.vacantes_disponibles, 0) AS vacantes_disponibles " +
                    "FROM municipio m " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "LEFT JOIN candidatos_por_municipio cp ON cp.id_municipio = m.id_municipio " +
                    "LEFT JOIN vacantes_por_municipio vp ON vp.id_municipio = m.id_municipio " +
                    "ORDER BY candidatos_desempleados DESC";

    public ArrayList<BrechaOfertaDemandaDTO> consultarBrechaPorAreaLaboral() {
        ArrayList<BrechaOfertaDemandaDTO> resultados =
                new ArrayList<BrechaOfertaDemandaDTO>();

        try (Connection connection = Conexion.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SELECT_BRECHA_POR_AREA)) {
            statement.setString(1, OfertaLaboral.ESTADO_ACTIVA);
            statement.setString(2, Candidato.ESTADO_DESEMPLEADO);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultados.add(new BrechaOfertaDemandaDTO(
                            resultSet.getString("area"),
                            resultSet.getInt("ofertas_activas"),
                            resultSet.getInt("candidatos_desempleados")));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar la brecha de oferta y demanda por área laboral.",
                    exception);
        }

        return resultados;
    }

    public ArrayList<ManoObraMunicipioDTO> consultarManoObraPorMunicipio() {
        ArrayList<ManoObraMunicipioDTO> resultados =
                new ArrayList<ManoObraMunicipioDTO>();

        try (Connection connection = Conexion.obtenerConexion();
             PreparedStatement statement =
                     connection.prepareStatement(SELECT_MANO_OBRA_POR_MUNICIPIO)) {
            statement.setString(1, Candidato.ESTADO_DESEMPLEADO);
            statement.setString(2, OfertaLaboral.ESTADO_ACTIVA);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultados.add(new ManoObraMunicipioDTO(
                            resultSet.getString("provincia"),
                            resultSet.getString("municipio"),
                            resultSet.getInt("candidatos_desempleados"),
                            resultSet.getInt("vacantes_disponibles")));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar la mano de obra desempleada por municipio.",
                    exception);
        }

        return resultados;
    }
}