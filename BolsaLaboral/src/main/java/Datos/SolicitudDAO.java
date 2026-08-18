package Datos;

import logico.Candidato;
import logico.OfertaLaboral;
import logico.Solicitud;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class SolicitudDAO {

    private static final String PREFIJO_CODIGO = "SOL-";
    private static final String PREFIJO_CODIGO_CANDIDATO = "CAN-";
    private static final String PREFIJO_CODIGO_OFERTA = "OFR-";

    private static final String SELECT_TODOS =
            "SELECT id_solicitud, estado, fechaSolicitud, fechaDecision, id_candidato, id_oferta " +
                    "FROM solicitudes " +
                    "ORDER BY fechaSolicitud DESC, id_solicitud DESC";

    private static final String INSERT =
            "INSERT INTO solicitudes (estado, fechaSolicitud, id_candidato, id_oferta) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_ESTADO =
            "UPDATE solicitudes SET estado = ?, fechaDecision = ? WHERE id_solicitud = ?";

    private static final String RECHAZAR_OTRAS_ENVIADAS =
            "UPDATE solicitudes SET estado = ?, fechaDecision = ? " +
                    "WHERE id_candidato = ? " +
                    "AND id_solicitud <> ? " +
                    "AND estado = ?";

    public ArrayList<Solicitud> listarTodos(ArrayList<Candidato> candidatosDisponibles,
                                            ArrayList<OfertaLaboral> ofertasDisponibles) {

        HashMap<String, Candidato> candidatosPorCodigo = new HashMap<>();
        if (candidatosDisponibles != null) {
            for (Candidato candidato : candidatosDisponibles) {
                if (candidato != null && candidato.getCodigo() != null) {
                    candidatosPorCodigo.put(candidato.getCodigo(), candidato);
                }
            }
        }

        HashMap<String, OfertaLaboral> ofertasPorCodigo = new HashMap<>();
        if (ofertasDisponibles != null) {
            for (OfertaLaboral oferta : ofertasDisponibles) {
                if (oferta != null && oferta.getCodigo() != null) {
                    ofertasPorCodigo.put(oferta.getCodigo(), oferta);
                }
            }
        }

        ArrayList<Solicitud> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idSolicitud = rs.getInt("id_solicitud");
                String codigoCandidato = PREFIJO_CODIGO_CANDIDATO + rs.getInt("id_candidato");
                String codigoOferta = PREFIJO_CODIGO_OFERTA + rs.getInt("id_oferta");

                Candidato candidato = candidatosPorCodigo.get(codigoCandidato);
                OfertaLaboral oferta = ofertasPorCodigo.get(codigoOferta);

                if (candidato == null || oferta == null) {
                    // Solicitud huérfana (su candidato u oferta ya no existe entre los
                    // cargados); se omite en vez de romper la carga completa.
                    continue;
                }

                Date fechaSolicitudSql = rs.getDate("fechaSolicitud");
                LocalDate fechaSolicitud = fechaSolicitudSql == null ? null : fechaSolicitudSql.toLocalDate();

                Solicitud solicitud = new Solicitud(
                        PREFIJO_CODIGO + idSolicitud,
                        fechaSolicitud,
                        rs.getString("estado"),
                        candidato,
                        oferta
                );

                resultado.add(solicitud);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo solicitudes desde la base de datos", e);
        }

        return resultado;
    }

    public void agregar(Solicitud solicitud) {
        int idCandidato = extraerId(solicitud.getSolicitante().getCodigo(),
                PREFIJO_CODIGO_CANDIDATO, "El candidato de la solicitud no tiene código.",
                "Código de candidato inválido: ");
        int idOferta = extraerId(solicitud.getOfertaSolicitada().getCodigo(),
                PREFIJO_CODIGO_OFERTA, "La oferta de la solicitud no tiene código.",
                "Código de oferta laboral inválido: ");

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, solicitud.getEstado());
            ps.setDate(2, Date.valueOf(solicitud.getFechaSolicitud()));
            ps.setInt(3, idCandidato);
            ps.setInt(4, idOferta);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se obtuvo el id generado para la solicitud.");
                }
                solicitud.setCodigo(PREFIJO_CODIGO + claves.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error agregando la solicitud a la base de datos", e);
        }
    }

    void actualizarEstado(
            Connection con,
            Solicitud solicitud,
            String nuevoEstado,
            LocalDate fechaDecision) throws SQLException {

        int idSolicitud = extraerIdDelCodigo(solicitud.getCodigo());

        try (PreparedStatement ps = con.prepareStatement(UPDATE_ESTADO)) {
            ps.setString(1, nuevoEstado);

            if (fechaDecision == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(fechaDecision));
            }

            ps.setInt(3, idSolicitud);

            int filasModificadas = ps.executeUpdate();

            if (filasModificadas == 0) {
                throw new SQLException(
                        "No existe una solicitud con id_solicitud = "
                                + idSolicitud + "."
                );
            }
        }
    }

    void rechazarOtrasSolicitudesEnviadas(
            Connection con,
            Candidato candidato,
            Solicitud solicitudAprobada,
            LocalDate fechaDecision) throws SQLException {

        int idCandidato = extraerId(
                candidato.getCodigo(),
                PREFIJO_CODIGO_CANDIDATO,
                "El candidato no tiene código.",
                "Código de candidato inválido: "
        );

        int idSolicitudAprobada =
                extraerIdDelCodigo(solicitudAprobada.getCodigo());

        try (PreparedStatement ps =
                     con.prepareStatement(RECHAZAR_OTRAS_ENVIADAS)) {

            ps.setString(1, Solicitud.ESTADO_RECHAZADA);

            if (fechaDecision == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(fechaDecision));
            }

            ps.setInt(3, idCandidato);
            ps.setInt(4, idSolicitudAprobada);
            ps.setString(5, Solicitud.ESTADO_ENVIADA);

            ps.executeUpdate();
        }
    }

    public void actualizarEstado(Solicitud solicitud, LocalDate fechaDecision) {
        try (Connection con = Conexion.obtenerConexion()) {
            actualizarEstado(
                    con,
                    solicitud,
                    solicitud.getEstado(),
                    fechaDecision
            );
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error actualizando el estado de la solicitud",
                    e
            );
        }
    }

    private int extraerIdDelCodigo(String codigo) {
        return extraerId(codigo, PREFIJO_CODIGO, "La solicitud no tiene código.",
                "Código de solicitud inválido: ");
    }

    private int extraerId(String codigo, String prefijo, String mensajeVacio, String mensajeInvalido) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalStateException(mensajeVacio);
        }

        String valorNumerico = codigo.trim();
        if (valorNumerico.startsWith(prefijo)) {
            valorNumerico = valorNumerico.substring(prefijo.length());
        }

        try {
            return Integer.parseInt(valorNumerico);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(mensajeInvalido + codigo, e);
        }
    }
}