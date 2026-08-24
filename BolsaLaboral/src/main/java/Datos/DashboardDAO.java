package Datos;

import logico.BrechaOfertaDemandaDTO;
import logico.Candidato;
import logico.CoberturaOfertaDTO;
import logico.ManoObraMunicipioDTO;
import logico.OfertaLaboral;
import logico.Solicitud;
import logico.TasaExitoCentroDTO;
import logico.TiempoResolucionAreaDTO;

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
                    "ocupadas_por_oferta AS ( " +
                    "SELECT s.id_oferta, COUNT(c.id_contratacion) AS ocupadas " +
                    "FROM solicitudes s " +
                    "JOIN Contrataciones c ON c.id_solicitud = s.id_solicitud " +
                    "GROUP BY s.id_oferta" +
                    "), " +
                    "vacantes_por_municipio AS ( " +
                    "SELECT ce.id_municipio, " +
                    "SUM(CASE WHEN o.vacantesTotales - COALESCE(oo.ocupadas, 0) > 0 " +
                    "THEN o.vacantesTotales - COALESCE(oo.ocupadas, 0) ELSE 0 END) " +
                    "AS vacantes_disponibles " +
                    "FROM centrosEmpleadores ce " +
                    "JOIN ofertas o ON o.id_centroEmpleador = ce.id_centroEmpleador " +
                    "LEFT JOIN ocupadas_por_oferta oo ON oo.id_oferta = o.id_oferta " +
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

    private static final String SELECT_TASA_EXITO_POR_CENTRO =
            "SELECT ce.nombre AS centro, " +
                    "COUNT(DISTINCT s.id_solicitud) AS oportunidades_enviadas, " +
                    "COUNT(DISTINCT c.id_contratacion) AS contrataciones, " +
                    "COALESCE(ROUND(" +
                    "CAST(COUNT(DISTINCT c.id_contratacion) AS DECIMAL(18, 4)) " +
                    "/ NULLIF(COUNT(DISTINCT s.id_solicitud), 0) * 100, 2" +
                    "), 0) AS tasa_conversion " +
                    "FROM centrosEmpleadores ce " +
                    "JOIN ofertas o ON o.id_centroEmpleador = ce.id_centroEmpleador " +
                    "JOIN solicitudes s ON s.id_oferta = o.id_oferta " +
                    "LEFT JOIN Contrataciones c ON c.id_solicitud = s.id_solicitud " +
                    "GROUP BY ce.id_centroEmpleador, ce.nombre " +
                    "HAVING COUNT(DISTINCT s.id_solicitud) > 0 " +
                    "ORDER BY tasa_conversion DESC, ce.nombre";

    private static final String SELECT_COBERTURA_OFERTAS =
            "SELECT o.puesto AS oferta, ce.nombre AS centro, al.nombre AS area, " +
                    "o.vacantesTotales AS vacantes_totales, " +
                    "COUNT(DISTINCT c.id_contratacion) AS vacantes_ocupadas, " +
                    "CASE WHEN o.vacantesTotales - COUNT(DISTINCT c.id_contratacion) > 0 " +
                    "THEN o.vacantesTotales - COUNT(DISTINCT c.id_contratacion) " +
                    "ELSE 0 END AS vacantes_pendientes, " +
                    "COUNT(DISTINCT s.id_solicitud) AS oportunidades_enviadas, " +
                    "CASE WHEN o.vacantesTotales > 0 THEN COALESCE(ROUND(" +
                    "CAST(COUNT(DISTINCT c.id_contratacion) AS DECIMAL(18, 4)) " +
                    "/ NULLIF(o.vacantesTotales, 0) * 100, 2), 0) " +
                    "ELSE 0 END AS porcentaje_cobertura " +
                    "FROM ofertas o " +
                    "JOIN centrosEmpleadores ce " +
                    "ON ce.id_centroEmpleador = o.id_centroEmpleador " +
                    "JOIN areasLaborales al ON al.id_areaLaboral = o.id_areaLaboral " +
                    "LEFT JOIN solicitudes s ON s.id_oferta = o.id_oferta " +
                    "LEFT JOIN Contrataciones c ON c.id_solicitud = s.id_solicitud " +
                    "WHERE o.estado = ? " +
                    "GROUP BY o.id_oferta, o.puesto, ce.nombre, al.nombre, o.vacantesTotales " +
                    "ORDER BY vacantes_pendientes DESC, oportunidades_enviadas ASC, o.id_oferta";

    private static final String SELECT_TIEMPO_RESOLUCION_POR_AREA =
            "SELECT al.nombre AS area, " +
                    "COUNT(DISTINCT s.id_solicitud) AS oportunidades_enviadas, " +
                    "COUNT(DISTINCT CASE WHEN s.fechaDecision IS NOT NULL " +
                    "THEN s.id_solicitud END) AS vinculaciones_resueltas, " +
                    "COUNT(DISTINCT CASE WHEN s.estado = ? " +
                    "THEN s.id_solicitud END) AS vinculaciones_pendientes, " +
                    "COUNT(DISTINCT CASE WHEN s.estado = ? " +
                    "AND DATEDIFF(DAY, s.fechaSolicitud, GETDATE()) > 7 " +
                    "THEN s.id_solicitud END) AS pendientes_mas_siete_dias, " +
                    "COALESCE(AVG(CASE WHEN s.fechaDecision IS NOT NULL " +
                    "THEN CAST(DATEDIFF(DAY, s.fechaSolicitud, s.fechaDecision) " +
                    "AS DECIMAL(10, 2)) END), 0) AS dias_promedio_resolucion, " +
                    "CASE WHEN COUNT(DISTINCT s.id_solicitud) > 0 THEN COALESCE(ROUND(" +
                    "CAST(COUNT(DISTINCT CASE WHEN s.fechaDecision IS NOT NULL " +
                    "THEN s.id_solicitud END) AS DECIMAL(18, 4)) " +
                    "/ NULLIF(COUNT(DISTINCT s.id_solicitud), 0) * 100, 2), 0) " +
                    "ELSE 0 END AS porcentaje_resolucion " +
                    "FROM areasLaborales al " +
                    "LEFT JOIN ofertas o ON o.id_areaLaboral = al.id_areaLaboral " +
                    "LEFT JOIN solicitudes s ON s.id_oferta = o.id_oferta " +
                    "GROUP BY al.id_areaLaboral, al.nombre " +
                    "ORDER BY pendientes_mas_siete_dias DESC, " +
                    "vinculaciones_pendientes DESC, al.nombre";

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

    public ArrayList<TasaExitoCentroDTO> consultarTasaExitoPorCentro() {
        ArrayList<TasaExitoCentroDTO> resultados = new ArrayList<TasaExitoCentroDTO>();

        try (Connection connection = Conexion.obtenerConexion();
             PreparedStatement statement =
                     connection.prepareStatement(SELECT_TASA_EXITO_POR_CENTRO)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultados.add(new TasaExitoCentroDTO(
                            resultSet.getString("centro"),
                            resultSet.getInt("oportunidades_enviadas"),
                            resultSet.getInt("contrataciones"),
                            resultSet.getDouble("tasa_conversion")));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar la conversión de oportunidades por centro empleador.",
                    exception);
        }

        return resultados;
    }

    public ArrayList<CoberturaOfertaDTO> consultarCoberturaOfertasActivas() {
        ArrayList<CoberturaOfertaDTO> resultados = new ArrayList<CoberturaOfertaDTO>();

        try (Connection connection = Conexion.obtenerConexion();
             PreparedStatement statement = connection.prepareStatement(SELECT_COBERTURA_OFERTAS)) {
            statement.setString(1, OfertaLaboral.ESTADO_ACTIVA);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultados.add(new CoberturaOfertaDTO(
                            resultSet.getString("oferta"),
                            resultSet.getString("centro"),
                            resultSet.getString("area"),
                            resultSet.getInt("vacantes_totales"),
                            resultSet.getInt("vacantes_ocupadas"),
                            resultSet.getInt("oportunidades_enviadas"),
                            resultSet.getDouble("porcentaje_cobertura")));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar la dificultad de cobertura de las ofertas activas.",
                    exception);
        }

        return resultados;
    }

    public ArrayList<TiempoResolucionAreaDTO> consultarTiempoResolucionPorArea() {
        ArrayList<TiempoResolucionAreaDTO> resultados =
                new ArrayList<TiempoResolucionAreaDTO>();

        try (Connection connection = Conexion.obtenerConexion();
             PreparedStatement statement =
                     connection.prepareStatement(SELECT_TIEMPO_RESOLUCION_POR_AREA)) {
            statement.setString(1, Solicitud.ESTADO_ENVIADA);
            statement.setString(2, Solicitud.ESTADO_ENVIADA);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resultados.add(new TiempoResolucionAreaDTO(
                            resultSet.getString("area"),
                            resultSet.getInt("oportunidades_enviadas"),
                            resultSet.getInt("vinculaciones_resueltas"),
                            resultSet.getInt("vinculaciones_pendientes"),
                            resultSet.getInt("pendientes_mas_siete_dias"),
                            resultSet.getDouble("dias_promedio_resolucion"),
                            resultSet.getDouble("porcentaje_resolucion")));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo consultar el tiempo de resolución por área laboral.",
                    exception);
        }

        return resultados;
    }
}
