package Datos;

import logico.BrechaOfertaDemandaDTO;
import logico.Candidato;
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
}
