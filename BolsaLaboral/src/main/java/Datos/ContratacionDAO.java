package Datos;

import logico.Solicitud;
import logico.VacanteCompletada;
import logico.Candidato;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ContratacionDAO {

    private static final String PREFIJO_CODIGO = "VAC-";
    private static final String PREFIJO_CODIGO_SOLICITUD = "SOL-";

    private static final String SELECT_TODOS =
            "SELECT id_contratacion, fechaContratacion, id_solicitud " +
                    "FROM Contrataciones " +
                    "ORDER BY fechaContratacion DESC, id_contratacion DESC";

    private static final String INSERT =
            "INSERT INTO Contrataciones (fechaContratacion, id_solicitud) VALUES (?, ?)";

    public ArrayList<VacanteCompletada> listarTodos(ArrayList<Solicitud> solicitudesDisponibles) {

        HashMap<String, Solicitud> solicitudesPorCodigo = new HashMap<>();
        if (solicitudesDisponibles != null) {
            for (Solicitud solicitud : solicitudesDisponibles) {
                if (solicitud != null && solicitud.getCodigo() != null) {
                    solicitudesPorCodigo.put(solicitud.getCodigo(), solicitud);
                }
            }
        }

        ArrayList<VacanteCompletada> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idContratacion = rs.getInt("id_contratacion");
                String codigoSolicitud = PREFIJO_CODIGO_SOLICITUD + rs.getInt("id_solicitud");

                Solicitud solicitud = solicitudesPorCodigo.get(codigoSolicitud);
                if (solicitud == null) {
                    continue;
                }

                Date fechaSql = rs.getDate("fechaContratacion");
                LocalDate fechaContratacion = fechaSql == null ? null : fechaSql.toLocalDate();

                VacanteCompletada vacante = new VacanteCompletada(
                        PREFIJO_CODIGO + idContratacion,
                        solicitud,
                        solicitud.getOfertaSolicitada(),
                        fechaContratacion
                );

                resultado.add(vacante);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo contrataciones desde la base de datos", e);
        }

        return resultado;
    }

    private int insertar(
            Connection con,
            VacanteCompletada vacante) throws SQLException {

        int idSolicitud = extraerId(
                vacante.getSolicitudAceptada().getCodigo(),
                PREFIJO_CODIGO_SOLICITUD,
                "La solicitud de la contratación no tiene código.",
                "Código de solicitud inválido: "
        );

        try (PreparedStatement ps = con.prepareStatement(
                INSERT,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setDate(
                    1,
                    Date.valueOf(vacante.getFechaContratacion())
            );
            ps.setInt(2, idSolicitud);

            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                            "No se obtuvo el id generado para la contratación."
                    );
                }

                return claves.getInt(1);
            }
        }
    }

    public void contratarAtomico(
            Solicitud solicitud,
            VacanteCompletada vacante) {

        validarContratacion(solicitud, vacante);

        Candidato candidato = solicitud.getSolicitante();
        LocalDate fechaDecision = vacante.getFechaContratacion();

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);

            try {
                SolicitudDAO solicitudDAO = new SolicitudDAO();
                CandidatoDAO candidatoDAO = new CandidatoDAO();

                solicitudDAO.actualizarEstado(
                        con,
                        solicitud,
                        Solicitud.ESTADO_APROBADA,
                        fechaDecision
                );

                solicitudDAO.rechazarOtrasSolicitudesEnviadas(
                        con,
                        candidato,
                        solicitud,
                        fechaDecision
                );

                int idContratacion = insertar(con, vacante);

                candidatoDAO.actualizarEstado(
                        con,
                        candidato,
                        Candidato.ESTADO_EMPLEADO
                );

                con.commit();

                vacante.setCodigo(
                        PREFIJO_CODIGO + idContratacion
                );

            } catch (SQLException | RuntimeException e) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error registrando la contratación completa",
                    e
            );
        }
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

    private void validarContratacion(
            Solicitud solicitud,
            VacanteCompletada vacante) {

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud es obligatoria."
            );
        }

        if (vacante == null) {
            throw new IllegalArgumentException(
                    "La contratación es obligatoria."
            );
        }

        if (solicitud.getSolicitante() == null) {
            throw new IllegalArgumentException(
                    "La solicitud no tiene candidato."
            );
        }

        if (solicitud.getOfertaSolicitada() == null) {
            throw new IllegalArgumentException(
                    "La solicitud no tiene oferta laboral."
            );
        }

        if (vacante.getSolicitudAceptada() == null) {
            throw new IllegalArgumentException(
                    "La contratación no tiene solicitud aceptada."
            );
        }

        if (vacante.getOfertaOcupada() == null) {
            throw new IllegalArgumentException(
                    "La contratación no tiene oferta laboral."
            );
        }

        if (vacante.getFechaContratacion() == null) {
            throw new IllegalArgumentException(
                    "La fecha de contratación es obligatoria."
            );
        }

        String codigoSolicitud = solicitud.getCodigo();
        String codigoSolicitudVacante =
                vacante.getSolicitudAceptada().getCodigo();

        if (codigoSolicitud == null
                || !codigoSolicitud.equals(codigoSolicitudVacante)) {
            throw new IllegalArgumentException(
                    "La solicitud aprobada no coincide con la solicitud "
                            + "de la contratación."
            );
        }

        String codigoOferta =
                solicitud.getOfertaSolicitada().getCodigo();
        String codigoOfertaVacante =
                vacante.getOfertaOcupada().getCodigo();

        if (codigoOferta == null
                || !codigoOferta.equals(codigoOfertaVacante)) {
            throw new IllegalArgumentException(
                    "La oferta de la solicitud no coincide con la oferta "
                            + "de la contratación."
            );
        }
    }
}