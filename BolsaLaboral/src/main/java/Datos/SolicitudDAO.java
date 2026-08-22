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

    private static final String SELECT_DUPLICADA =
            "SELECT COUNT(*) FROM solicitudes WITH (UPDLOCK, HOLDLOCK) " +
                    "WHERE id_candidato = ? AND id_oferta = ?";

    private static final String APROBAR_ENVIADA =
            "UPDATE solicitudes SET estado = ?, fechaDecision = ? " +
                    "WHERE id_solicitud = ? AND estado = ?";

    private static final String RECHAZAR_ENVIADA =
            "UPDATE solicitudes SET estado = ?, fechaDecision = ? " +
                    "WHERE id_solicitud = ? AND id_candidato = ? AND estado = ?";

    private static final String SELECT_ESTADOS_CANDIDATO =
            "SELECT estado FROM solicitudes WITH (UPDLOCK, HOLDLOCK) WHERE id_candidato = ?";

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
                Date fechaDecisionSql = rs.getDate("fechaDecision");
                LocalDate fechaDecision = fechaDecisionSql == null ? null : fechaDecisionSql.toLocalDate();

                Solicitud solicitud = new Solicitud(
                        PREFIJO_CODIGO + idSolicitud,
                        fechaSolicitud,
                        fechaDecision,
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

    public void vincularAtomico(Solicitud solicitud) {
        validarNuevaSolicitud(solicitud);

        int idCandidato = extraerId(solicitud.getSolicitante().getCodigo(),
                PREFIJO_CODIGO_CANDIDATO, "El candidato de la solicitud no tiene código.",
                "Código de candidato inválido: ");
        int idOferta = extraerId(solicitud.getOfertaSolicitada().getCodigo(),
                PREFIJO_CODIGO_OFERTA, "La oferta de la solicitud no tiene código.",
                "Código de oferta laboral inválido: ");
        int idSolicitudGenerada = 0;
        boolean transaccionConfirmada = false;

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                CandidatoDAO candidatoDAO = new CandidatoDAO();
                candidatoDAO.bloquearParaProcesamiento(
                        con,
                        solicitud.getSolicitante()
                );
                validarNoDuplicada(con, idCandidato, idOferta);
                idSolicitudGenerada = insertar(con, solicitud, idCandidato, idOferta);
                candidatoDAO.actualizarEstadoParaVinculacion(
                        con,
                        solicitud.getSolicitante()
                );

                con.commit();
                transaccionConfirmada = true;
            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        } catch (SQLException e) {
            if (!transaccionConfirmada) {
                throw new RuntimeException("Error vinculando la solicitud y el candidato", e);
            }
        }

        solicitud.setCodigo(PREFIJO_CODIGO + idSolicitudGenerada);
    }

    public String rechazarAtomico(Solicitud solicitud, LocalDate fechaDecision) {
        validarRechazo(solicitud, fechaDecision);

        int idSolicitud = extraerIdDelCodigo(solicitud.getCodigo());
        int idCandidato = extraerId(
                solicitud.getSolicitante().getCodigo(),
                PREFIJO_CODIGO_CANDIDATO,
                "El candidato de la solicitud no tiene código.",
                "Código de candidato inválido: "
        );
        String estadoCandidato = null;
        boolean transaccionConfirmada = false;

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                CandidatoDAO candidatoDAO = new CandidatoDAO();
                candidatoDAO.bloquearParaProcesamiento(
                        con,
                        solicitud.getSolicitante()
                );
                rechazarSolicitudEnviada(
                        con,
                        idSolicitud,
                        idCandidato,
                        fechaDecision
                );
                estadoCandidato = determinarEstadoCandidato(con, idCandidato);
                candidatoDAO.actualizarEstado(
                        con,
                        solicitud.getSolicitante(),
                        estadoCandidato
                );

                con.commit();
                transaccionConfirmada = true;
            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        } catch (SQLException e) {
            if (!transaccionConfirmada) {
                throw new RuntimeException("Error rechazando la solicitud y actualizando el candidato", e);
            }
        }

        return estadoCandidato;
    }

    private int insertar(
            Connection con,
            Solicitud solicitud,
            int idCandidato,
            int idOferta) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, solicitud.getEstado());
            ps.setDate(2, Date.valueOf(solicitud.getFechaSolicitud()));
            ps.setInt(3, idCandidato);
            ps.setInt(4, idOferta);

            int filasInsertadas = ps.executeUpdate();
            if (filasInsertadas != 1) {
                throw new SQLException("No se insertó exactamente una solicitud.");
            }

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se obtuvo el id generado para la solicitud.");
                }
                return claves.getInt(1);
            }
        }
    }

    private void validarNoDuplicada(
            Connection con,
            int idCandidato,
            int idOferta) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(SELECT_DUPLICADA)) {
            ps.setInt(1, idCandidato);
            ps.setInt(2, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo comprobar si la solicitud está duplicada.");
                }
                if (rs.getInt(1) > 0) {
                    throw new SQLException(
                            "Ya existe una solicitud para este candidato y esta oferta."
                    );
                }
            }
        }
    }

    private void rechazarSolicitudEnviada(
            Connection con,
            int idSolicitud,
            int idCandidato,
            LocalDate fechaDecision) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(RECHAZAR_ENVIADA)) {
            ps.setString(1, Solicitud.ESTADO_RECHAZADA);
            ps.setDate(2, Date.valueOf(fechaDecision));
            ps.setInt(3, idSolicitud);
            ps.setInt(4, idCandidato);
            ps.setString(5, Solicitud.ESTADO_ENVIADA);

            int filasModificadas = ps.executeUpdate();
            if (filasModificadas != 1) {
                throw new SQLException(
                        "La solicitud no existe, no pertenece al candidato o ya fue procesada."
                );
            }
        }
    }

    private String determinarEstadoCandidato(Connection con, int idCandidato) throws SQLException {
        boolean tieneAprobada = false;
        boolean tieneEnviada = false;

        try (PreparedStatement ps = con.prepareStatement(SELECT_ESTADOS_CANDIDATO)) {
            ps.setInt(1, idCandidato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString("estado");
                    String normalizado = estado == null ? null : estado.trim();
                    if (Solicitud.esEstadoAprobada(normalizado)) {
                        tieneAprobada = true;
                    } else if (Solicitud.ESTADO_ENVIADA.equals(normalizado)) {
                        tieneEnviada = true;
                    }
                }
            }
        }

        if (tieneAprobada) {
            return Candidato.ESTADO_EMPLEADO;
        }
        if (tieneEnviada) {
            return Candidato.ESTADO_EN_ESPERA;
        }
        return Candidato.ESTADO_DESEMPLEADO;
    }

    void aprobarEnviada(
            Connection con,
            Solicitud solicitud,
            LocalDate fechaDecision) throws SQLException {

        int idSolicitud = extraerIdDelCodigo(solicitud.getCodigo());

        try (PreparedStatement ps = con.prepareStatement(APROBAR_ENVIADA)) {
            ps.setString(1, Solicitud.ESTADO_APROBADA);
            ps.setDate(2, Date.valueOf(fechaDecision));
            ps.setInt(3, idSolicitud);
            ps.setString(4, Solicitud.ESTADO_ENVIADA);

            int filasModificadas = ps.executeUpdate();

            if (filasModificadas != 1) {
                throw new SQLException(
                        "La solicitud no existe o ya no se encuentra enviada."
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

    private void validarNuevaSolicitud(Solicitud solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        if (solicitud.getCodigo() != null) {
            throw new IllegalArgumentException("Una solicitud nueva no puede tener código asignado.");
        }
        if (solicitud.getFechaSolicitud() == null) {
            throw new IllegalArgumentException("La fecha de solicitud es obligatoria.");
        }
        if (!Solicitud.ESTADO_ENVIADA.equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("Una solicitud nueva debe estar enviada.");
        }
        if (solicitud.getFechaDecision() != null) {
            throw new IllegalArgumentException("Una solicitud nueva no puede tener fecha de decisión.");
        }
        if (solicitud.getSolicitante() == null) {
            throw new IllegalArgumentException("La solicitud no tiene candidato.");
        }
        if (solicitud.getOfertaSolicitada() == null) {
            throw new IllegalArgumentException("La solicitud no tiene oferta laboral.");
        }
        String estadoCandidato = solicitud.getSolicitante().getEstado();
        if (!Candidato.ESTADO_DESEMPLEADO.equals(estadoCandidato)
                && !Candidato.ESTADO_EN_ESPERA.equals(estadoCandidato)) {
            throw new IllegalArgumentException(
                    "Solo un candidato desempleado o en espera puede crear solicitudes."
            );
        }
    }

    private void validarRechazo(Solicitud solicitud, LocalDate fechaDecision) {
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        if (!Solicitud.ESTADO_ENVIADA.equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("Solo se puede rechazar una solicitud enviada.");
        }
        if (solicitud.getSolicitante() == null) {
            throw new IllegalArgumentException("La solicitud no tiene candidato.");
        }
        if (fechaDecision == null) {
            throw new IllegalArgumentException("La fecha de decisión es obligatoria.");
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
