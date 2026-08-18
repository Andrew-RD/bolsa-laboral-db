package Datos;

import exception.NotRemovableException;
import logico.Candidato;
import logico.Obrero;
import logico.SituacionAcademica;
import logico.TecnicoSuperior;
import logico.Universitario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CandidatoDAO {

    private static final String PREFIJO_CODIGO = "CAN-";

    private static final String SELECT_BASE =
            "SELECT c.id_candidato, c.identificacion, c.nombre, c.apellido, " +
                    "c.fechaNacimiento, c.genero, c.telefono, c.correo, " +
                    "c.jornada, c.modalidad, c.aspiracionSalarial, " +
                    "c.licenciaConducir, c.disposicionMudarse, c.estado, " +
                    "al.nombre AS areaLaboral, " +
                    "p.nombre AS provincia, m.nombre AS municipio, " +
                    "uni.nombre AS universidad, " +
                    "car.nombre AS carrera, " +
                    "u.nivelAcademico, u.situacionAcademica, " +
                    "atec.nombre AS areaTecnica, " +
                    "t.añosExperiencia, " +
                    "CASE WHEN u.id_candidato IS NOT NULL THEN 'UNIVERSITARIO' " +
                    "WHEN t.id_candidato IS NOT NULL THEN 'TECNICO' " +
                    "WHEN o.id_candidato IS NOT NULL THEN 'OBRERO' END AS tipoCandidato " +
                    "FROM candidatos c " +
                    "JOIN areasLaborales al ON al.id_areaLaboral = c.id_areaLaboral " +
                    "JOIN municipio m ON m.id_municipio = c.id_municipio " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "LEFT JOIN universitarios u ON u.id_candidato = c.id_candidato " +
                    "LEFT JOIN universidades uni ON uni.id_universidad = u.id_universidad " +
                    "LEFT JOIN requerimientos car ON car.id_requerimiento = u.id_requerimiento " +
                    "LEFT JOIN tecnicos t ON t.id_candidato = c.id_candidato " +
                    "LEFT JOIN requerimientos atec ON atec.id_requerimiento = t.id_requerimiento " +
                    "LEFT JOIN obreros o ON o.id_candidato = c.id_candidato";

    private static final String SELECT_TODOS =
            SELECT_BASE + " ORDER BY c.nombre, c.apellido";

    private static final String SELECT_POR_IDENTIFICACION =
            SELECT_BASE + " WHERE c.identificacion = ?";

    private static final String SELECT_IDIOMAS_TODOS =
            "SELECT ci.id_candidato, i.nombre " +
                    "FROM candidatos_idiomas ci " +
                    "JOIN idiomas i ON i.id_idioma = ci.id_idioma";

    private static final String SELECT_IDIOMAS_POR_CANDIDATO =
            "SELECT i.nombre " +
                    "FROM candidatos_idiomas ci " +
                    "JOIN idiomas i ON i.id_idioma = ci.id_idioma " +
                    "WHERE ci.id_candidato = ?";

    private static final String SELECT_HABILIDADES_TODOS =
            "SELECT oh.id_candidato, h.nombre " +
                    "FROM obreros_habilidades oh " +
                    "JOIN requerimientos h ON h.id_requerimiento = oh.id_requerimiento";

    private static final String SELECT_HABILIDADES_POR_CANDIDATO =
            "SELECT h.nombre " +
                    "FROM obreros_habilidades oh " +
                    "JOIN requerimientos h ON h.id_requerimiento = oh.id_requerimiento " +
                    "WHERE oh.id_candidato = ?";

    private static final String INSERT_CANDIDATO =
            "INSERT INTO candidatos " +
                    "(identificacion, nombre, apellido, fechaNacimiento, genero, telefono, correo, " +
                    "jornada, modalidad, aspiracionSalarial, licenciaConducir, disposicionMudarse, " +
                    "estado, id_areaLaboral, id_municipio) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_CANDIDATO =
            "UPDATE candidatos SET " +
                    "identificacion = ?, nombre = ?, apellido = ?, fechaNacimiento = ?, genero = ?, " +
                    "telefono = ?, correo = ?, jornada = ?, modalidad = ?, aspiracionSalarial = ?, " +
                    "licenciaConducir = ?, disposicionMudarse = ?, estado = ?, id_areaLaboral = ?, " +
                    "id_municipio = ? WHERE id_candidato = ?";

    private static final String INSERT_UNIVERSITARIO =
            "INSERT INTO universitarios " +
                    "(id_candidato, id_universidad, id_requerimiento, nivelAcademico, situacionAcademica) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_UNIVERSITARIO =
            "UPDATE universitarios SET id_universidad = ?, id_requerimiento = ?, " +
                    "nivelAcademico = ?, situacionAcademica = ? WHERE id_candidato = ?";

    private static final String INSERT_TECNICO =
            "INSERT INTO tecnicos (id_candidato, añosExperiencia, id_requerimiento) VALUES (?, ?, ?)";

    private static final String UPDATE_TECNICO =
            "UPDATE tecnicos SET añosExperiencia = ?, id_requerimiento = ? WHERE id_candidato = ?";

    private static final String INSERT_OBRERO =
            "INSERT INTO obreros (id_candidato) VALUES (?)";

    private static final String INSERT_IDIOMA_CANDIDATO =
            "INSERT INTO candidatos_idiomas (id_candidato, id_idioma) VALUES (?, ?)";

    private static final String DELETE_IDIOMAS_CANDIDATO =
            "DELETE FROM candidatos_idiomas WHERE id_candidato = ?";

    private static final String INSERT_HABILIDAD_OBRERO =
            "INSERT INTO obreros_habilidades (id_candidato, id_requerimiento) VALUES (?, ?)";

    private static final String DELETE_HABILIDADES_OBRERO =
            "DELETE FROM obreros_habilidades WHERE id_candidato = ?";

    private static final String DELETE_UNIVERSITARIO =
            "DELETE FROM universitarios WHERE id_candidato = ?";

    private static final String DELETE_TECNICO =
            "DELETE FROM tecnicos WHERE id_candidato = ?";

    private static final String DELETE_OBRERO =
            "DELETE FROM obreros WHERE id_candidato = ?";

    private static final String DELETE_CANDIDATO =
            "DELETE FROM candidatos WHERE id_candidato = ?";

    private static final String COUNT_SOLICITUDES =
            "SELECT COUNT(*) FROM solicitudes WHERE id_candidato = ?";

    private static final String DELETE_CONTRATACIONES_DEL_CANDIDATO =
            "DELETE FROM Contrataciones WHERE id_solicitud IN " +
                    "(SELECT id_solicitud FROM solicitudes WHERE id_candidato = ?)";

    private static final String DELETE_SOLICITUDES_DEL_CANDIDATO =
            "DELETE FROM solicitudes WHERE id_candidato = ?";

    private static final String SELECT_ID_AREA_LABORAL =
            "SELECT id_areaLaboral FROM areasLaborales WHERE nombre = ?";

    private static final String SELECT_ID_MUNICIPIO =
            "SELECT m.id_municipio " +
                    "FROM municipio m " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "WHERE m.nombre = ? AND p.nombre = ?";

    private static final String SELECT_ID_UNIVERSIDAD =
            "SELECT id_universidad FROM universidades WHERE nombre = ?";

    private static final String SELECT_ID_IDIOMA =
            "SELECT id_idioma FROM idiomas WHERE nombre = ?";

    private static final String UPDATE_ESTADO =
            "UPDATE candidatos SET estado = ? WHERE id_candidato = ?";

    void actualizarEstado(
            Connection con,
            Candidato candidato,
            String nuevoEstado) throws SQLException {

        int idCandidato = extraerIdDelCodigo(candidato.getCodigo());

        try (PreparedStatement ps = con.prepareStatement(UPDATE_ESTADO)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCandidato);

            int filasModificadas = ps.executeUpdate();

            if (filasModificadas == 0) {
                throw new SQLException(
                        "No existe un candidato con id_candidato = "
                                + idCandidato + "."
                );
            }
        }
    }

    public ArrayList<Candidato> listarTodos() {
        ArrayList<Candidato> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion()) {

            LinkedHashMap<Integer, ArrayList<String>> idiomasPorCandidato = cargarIdiomasPorCandidato(con);
            LinkedHashMap<Integer, ArrayList<String>> habilidadesPorCandidato = cargarHabilidadesPorCandidato(con);

            try (PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    resultado.add(mapearCandidato(rs, idiomasPorCandidato, habilidadesPorCandidato));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo candidatos desde la base de datos", e);
        }

        return resultado;
    }

    public Candidato buscarPorIdentificacion(String identificacion) {
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_IDENTIFICACION)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idCandidato = rs.getInt("id_candidato");
                    ArrayList<String> idiomas = obtenerIdiomas(con, idCandidato);
                    ArrayList<String> habilidades = obtenerHabilidades(con, idCandidato);
                    return construirCandidato(rs, idCandidato, idiomas, habilidades);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error buscando el candidato por identificación", e);
        }

        return null;
    }

    public void agregar(Candidato candidato) {
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idAreaLaboral = buscarIdAreaLaboral(con, candidato.getAreaDeInteres());
                int idMunicipio = buscarIdMunicipio(con, candidato.getMunicipio(), candidato.getProvincia());

                int idCandidato;
                try (PreparedStatement ps = con.prepareStatement(INSERT_CANDIDATO, Statement.RETURN_GENERATED_KEYS)) {
                    vincularDatosCandidato(ps, candidato, idAreaLaboral, idMunicipio);
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("No se obtuvo el id generado para el candidato.");
                        }
                        idCandidato = claves.getInt(1);
                    }
                }

                insertarSubtipo(con, candidato, idCandidato);
                insertarIdiomas(con, idCandidato, candidato.getIdiomas());

                con.commit();
                candidato.setCodigo(crearCodigo(idCandidato));
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando el candidato a la base de datos", e);
        }
    }

    public void modificar(Candidato candidato) {
        int idCandidato = extraerIdDelCodigo(candidato.getCodigo());

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idAreaLaboral = buscarIdAreaLaboral(con, candidato.getAreaDeInteres());
                int idMunicipio = buscarIdMunicipio(con, candidato.getMunicipio(), candidato.getProvincia());

                try (PreparedStatement ps = con.prepareStatement(UPDATE_CANDIDATO)) {
                    vincularDatosCandidato(ps, candidato, idAreaLaboral, idMunicipio);
                    ps.setInt(16, idCandidato);

                    int filas = ps.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("No existe un candidato con id_candidato = "
                                + idCandidato + ".");
                    }
                }

                actualizarSubtipo(con, candidato, idCandidato);
                reemplazarIdiomas(con, idCandidato, candidato.getIdiomas());

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error modificando el candidato en la base de datos", e);
        }
    }

    public int contarSolicitudes(Candidato candidato) {
        int idCandidato = extraerIdDelCodigo(candidato.getCodigo());

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(COUNT_SOLICITUDES)) {

            ps.setInt(1, idCandidato);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error contando las solicitudes del candidato", e);
        }
        return 0;
    }

    public void eliminar(Candidato candidato) throws NotRemovableException {
        int idCandidato = extraerIdDelCodigo(candidato.getCodigo());

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                eliminarSolicitudesYVinculos(con, idCandidato);
                eliminarSubtipoYVinculos(con, idCandidato);

                try (PreparedStatement ps = con.prepareStatement(DELETE_CANDIDATO)) {
                    ps.setInt(1, idCandidato);
                    int filas = ps.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("No existe un candidato con id_candidato = "
                                + idCandidato + ".");
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                if (esViolacionDeIntegridadReferencial(e)) {
                    throw new NotRemovableException(
                            "El candidato no puede ser eliminado porque tiene información vinculada que no pudo eliminarse automáticamente.",
                            e);
                }
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando el candidato de la base de datos", e);
        }
    }

    private void eliminarSolicitudesYVinculos(Connection con, int idCandidato) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_CONTRATACIONES_DEL_CANDIDATO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_SOLICITUDES_DEL_CANDIDATO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
    }

    private boolean esViolacionDeIntegridadReferencial(SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState != null && sqlState.startsWith("23")) {
            return true;
        }
        String mensaje = e.getMessage();
        return mensaje != null
                && (mensaje.contains("REFERENCE constraint")
                || mensaje.contains("FOREIGN KEY constraint")
                || mensaje.contains("conflicted with the"));
    }

    private void eliminarSubtipoYVinculos(Connection con, int idCandidato) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_IDIOMAS_CANDIDATO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_HABILIDADES_OBRERO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_UNIVERSITARIO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_TECNICO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_OBRERO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
    }

    private void vincularDatosCandidato(
            PreparedStatement ps,
            Candidato candidato,
            int idAreaLaboral,
            int idMunicipio) throws SQLException {

        ps.setString(1, candidato.getIdentificacion());
        ps.setString(2, candidato.getNombres());
        ps.setString(3, candidato.getApellidos());
        ps.setDate(4, Date.valueOf(candidato.getFechaNacimiento()));
        ps.setString(5, candidato.getGenero());
        ps.setString(6, candidato.getTelefono());
        ps.setString(7, candidato.getCorreo());
        ps.setString(8, candidato.getJornada());
        ps.setString(9, candidato.getModalidad());
        ps.setFloat(10, candidato.getAspiracionSalarial());
        ps.setBoolean(11, candidato.isLicenciaConducir());
        ps.setBoolean(12, candidato.isDisposicionMudarse());
        ps.setString(13, candidato.getEstado());
        ps.setInt(14, idAreaLaboral);
        ps.setInt(15, idMunicipio);
    }

    private void insertarSubtipo(Connection con, Candidato candidato, int idCandidato) throws SQLException {
        if (candidato instanceof Universitario) {
            Universitario universitario = (Universitario) candidato;
            int idUniversidad = buscarIdUniversidad(con, universitario.getUniversidad());
            int idCarrera = buscarIdRequerimiento(con, "carreras", universitario.getCarrera());

            try (PreparedStatement ps = con.prepareStatement(INSERT_UNIVERSITARIO)) {
                ps.setInt(1, idCandidato);
                ps.setInt(2, idUniversidad);
                ps.setInt(3, idCarrera);
                ps.setString(4, universitario.getNivelAcademico());
                ps.setString(5, universitario.getSituacionAcademica().getEtiqueta());
                ps.executeUpdate();
            }

        } else if (candidato instanceof TecnicoSuperior) {
            TecnicoSuperior tecnico = (TecnicoSuperior) candidato;
            int idAreaTecnica = buscarIdRequerimiento(con, "areasTecnicas", tecnico.getAreaTecnica());

            try (PreparedStatement ps = con.prepareStatement(INSERT_TECNICO)) {
                ps.setInt(1, idCandidato);
                ps.setInt(2, tecnico.getAniosExperiencia());
                ps.setInt(3, idAreaTecnica);
                ps.executeUpdate();
            }

        } else if (candidato instanceof Obrero) {
            try (PreparedStatement ps = con.prepareStatement(INSERT_OBRERO)) {
                ps.setInt(1, idCandidato);
                ps.executeUpdate();
            }
            insertarHabilidades(con, idCandidato, ((Obrero) candidato).getHabilidades());

        } else {
            throw new SQLException("Tipo de candidato no soportado: " + candidato.getClass().getSimpleName());
        }
    }

    private void actualizarSubtipo(Connection con, Candidato candidato, int idCandidato) throws SQLException {
        if (candidato instanceof Universitario) {
            Universitario universitario = (Universitario) candidato;
            int idUniversidad = buscarIdUniversidad(con, universitario.getUniversidad());
            int idCarrera = buscarIdRequerimiento(con, "carreras", universitario.getCarrera());

            try (PreparedStatement ps = con.prepareStatement(UPDATE_UNIVERSITARIO)) {
                ps.setInt(1, idUniversidad);
                ps.setInt(2, idCarrera);
                ps.setString(3, universitario.getNivelAcademico());
                ps.setString(4, universitario.getSituacionAcademica().getEtiqueta());
                ps.setInt(5, idCandidato);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe un universitario con id_candidato = "
                            + idCandidato + ".");
                }
            }

        } else if (candidato instanceof TecnicoSuperior) {
            TecnicoSuperior tecnico = (TecnicoSuperior) candidato;
            int idAreaTecnica = buscarIdRequerimiento(con, "areasTecnicas", tecnico.getAreaTecnica());

            try (PreparedStatement ps = con.prepareStatement(UPDATE_TECNICO)) {
                ps.setInt(1, tecnico.getAniosExperiencia());
                ps.setInt(2, idAreaTecnica);
                ps.setInt(3, idCandidato);

                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No existe un técnico con id_candidato = "
                            + idCandidato + ".");
                }
            }

        } else if (candidato instanceof Obrero) {
            reemplazarHabilidades(con, idCandidato, ((Obrero) candidato).getHabilidades());

        } else {
            throw new SQLException("Tipo de candidato no soportado: " + candidato.getClass().getSimpleName());
        }
    }

    private void insertarIdiomas(Connection con, int idCandidato, ArrayList<String> idiomas) throws SQLException {
        if (idiomas == null || idiomas.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_IDIOMA_CANDIDATO)) {
            for (String nombreIdioma : idiomas) {
                int idIdioma = buscarIdIdioma(con, nombreIdioma);
                ps.setInt(1, idCandidato);
                ps.setInt(2, idIdioma);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void reemplazarIdiomas(Connection con, int idCandidato, ArrayList<String> idiomas) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_IDIOMAS_CANDIDATO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        insertarIdiomas(con, idCandidato, idiomas);
    }

    private void insertarHabilidades(Connection con, int idCandidato, ArrayList<String> habilidades) throws SQLException {
        if (habilidades == null || habilidades.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_HABILIDAD_OBRERO)) {
            for (String nombreHabilidad : habilidades) {
                int idHabilidad = buscarIdRequerimiento(con, "habilidades", nombreHabilidad);
                ps.setInt(1, idCandidato);
                ps.setInt(2, idHabilidad);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void reemplazarHabilidades(Connection con, int idCandidato, ArrayList<String> habilidades) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_HABILIDADES_OBRERO)) {
            ps.setInt(1, idCandidato);
            ps.executeUpdate();
        }
        insertarHabilidades(con, idCandidato, habilidades);
    }

    private LinkedHashMap<Integer, ArrayList<String>> cargarIdiomasPorCandidato(Connection con) throws SQLException {
        LinkedHashMap<Integer, ArrayList<String>> resultado = new LinkedHashMap<>();

        try (PreparedStatement ps = con.prepareStatement(SELECT_IDIOMAS_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idCandidato = rs.getInt("id_candidato");
                resultado.computeIfAbsent(idCandidato, k -> new ArrayList<>()).add(rs.getString("nombre"));
            }
        }
        return resultado;
    }

    private LinkedHashMap<Integer, ArrayList<String>> cargarHabilidadesPorCandidato(Connection con) throws SQLException {
        LinkedHashMap<Integer, ArrayList<String>> resultado = new LinkedHashMap<>();

        try (PreparedStatement ps = con.prepareStatement(SELECT_HABILIDADES_TODOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idCandidato = rs.getInt("id_candidato");
                resultado.computeIfAbsent(idCandidato, k -> new ArrayList<>()).add(rs.getString("nombre"));
            }
        }
        return resultado;
    }

    private ArrayList<String> obtenerIdiomas(Connection con, int idCandidato) throws SQLException {
        ArrayList<String> resultado = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(SELECT_IDIOMAS_POR_CANDIDATO)) {
            ps.setInt(1, idCandidato);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getString("nombre"));
                }
            }
        }
        return resultado;
    }

    private ArrayList<String> obtenerHabilidades(Connection con, int idCandidato) throws SQLException {
        ArrayList<String> resultado = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(SELECT_HABILIDADES_POR_CANDIDATO)) {
            ps.setInt(1, idCandidato);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getString("nombre"));
                }
            }
        }
        return resultado;
    }

    private int buscarIdAreaLaboral(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_AREA_LABORAL)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_areaLaboral");
                }
            }
        }
        throw new SQLException("No existe el área laboral '" + nombre + "'.");
    }

    private int buscarIdMunicipio(Connection con, String nombreMunicipio, String nombreProvincia) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_MUNICIPIO)) {
            ps.setString(1, nombreMunicipio);
            ps.setString(2, nombreProvincia);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_municipio");
                }
            }
        }
        throw new SQLException("No existe el municipio '" + nombreMunicipio
                + "' en la provincia '" + nombreProvincia + "'.");
    }

    private int buscarIdUniversidad(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_UNIVERSIDAD)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_universidad");
                }
            }
        }
        throw new SQLException("No existe la universidad '" + nombre + "'.");
    }

    private int buscarIdRequerimiento(Connection con, String tablaSubtipo, String nombre) throws SQLException {
        String sql = "SELECT r.id_requerimiento FROM requerimientos r "
                + "JOIN " + tablaSubtipo + " s ON s.id_requerimiento = r.id_requerimiento "
                + "WHERE r.nombre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_requerimiento");
                }
            }
        }
        throw new SQLException("No existe '" + nombre + "' en " + tablaSubtipo + ".");
    }

    private int buscarIdIdioma(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_ID_IDIOMA)) {
            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_idioma");
                }
            }
        }
        throw new SQLException("No existe el idioma '" + nombre + "'.");
    }

    private Candidato mapearCandidato(
            ResultSet rs,
            LinkedHashMap<Integer, ArrayList<String>> idiomasPorCandidato,
            LinkedHashMap<Integer, ArrayList<String>> habilidadesPorCandidato) throws SQLException {

        int idCandidato = rs.getInt("id_candidato");
        ArrayList<String> idiomas = idiomasPorCandidato.getOrDefault(idCandidato, new ArrayList<>());
        ArrayList<String> habilidades = habilidadesPorCandidato.getOrDefault(idCandidato, new ArrayList<>());

        return construirCandidato(rs, idCandidato, idiomas, habilidades);
    }

    private Candidato construirCandidato(
            ResultSet rs,
            int idCandidato,
            ArrayList<String> idiomas,
            ArrayList<String> habilidades) throws SQLException {

        String codigo = crearCodigo(idCandidato);
        String identificacion = rs.getString("identificacion");
        String nombres = rs.getString("nombre");
        String apellidos = rs.getString("apellido");
        LocalDate fechaNacimiento = rs.getDate("fechaNacimiento").toLocalDate();
        String genero = rs.getString("genero");
        String provincia = rs.getString("provincia");
        String municipio = rs.getString("municipio");
        String telefono = rs.getString("telefono");
        String correo = rs.getString("correo");
        String jornada = rs.getString("jornada");
        String modalidad = rs.getString("modalidad");
        String areaDeInteres = rs.getString("areaLaboral");
        float aspiracionSalarial = rs.getFloat("aspiracionSalarial");
        boolean licenciaConducir = rs.getBoolean("licenciaConducir");
        boolean disposicionMudarse = rs.getBoolean("disposicionMudarse");
        String estado = rs.getString("estado");

        String tipoCandidato = rs.getString("tipoCandidato");

        if ("UNIVERSITARIO".equals(tipoCandidato)) {
            String universidad = rs.getString("universidad");
            String carrera = rs.getString("carrera");
            String nivelAcademico = rs.getString("nivelAcademico");
            SituacionAcademica situacionAcademica = mapearSituacionAcademica(rs.getString("situacionAcademica"));

            return new Universitario(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero,
                    provincia, municipio, telefono, correo, jornada, modalidad, areaDeInteres,
                    aspiracionSalarial, licenciaConducir, disposicionMudarse, idiomas,
                    universidad, carrera, nivelAcademico, situacionAcademica, estado);

        } else if ("TECNICO".equals(tipoCandidato)) {
            String areaTecnica = rs.getString("areaTecnica");
            int aniosExperiencia = rs.getInt("añosExperiencia");

            return new TecnicoSuperior(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero,
                    provincia, municipio, telefono, correo, jornada, modalidad, areaDeInteres,
                    aspiracionSalarial, licenciaConducir, disposicionMudarse, idiomas,
                    areaTecnica, aniosExperiencia, estado);

        } else if ("OBRERO".equals(tipoCandidato)) {
            return new Obrero(codigo, identificacion, nombres, apellidos, fechaNacimiento, genero,
                    provincia, municipio, telefono, correo, jornada, modalidad, areaDeInteres,
                    aspiracionSalarial, licenciaConducir, disposicionMudarse, idiomas, habilidades, estado);

        } else {
            throw new SQLException("El candidato con id_candidato = " + idCandidato
                    + " no tiene un subtipo reconocido en la base de datos.");
        }
    }

    private SituacionAcademica mapearSituacionAcademica(String valor) {
        if (valor != null) {
            for (SituacionAcademica situacion : SituacionAcademica.values()) {
                if (situacion.getEtiqueta().equalsIgnoreCase(valor.trim())) {
                    return situacion;
                }
            }
        }
        return SituacionAcademica.NO_ESPECIFICADO;
    }

    private String crearCodigo(int idCandidato) {
        return PREFIJO_CODIGO + idCandidato;
    }

    private int extraerIdDelCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalStateException("El candidato no tiene código.");
        }

        String valorNumerico = codigo.trim();

        if (valorNumerico.startsWith(PREFIJO_CODIGO)) {
            valorNumerico = valorNumerico.substring(PREFIJO_CODIGO.length());
        }

        try {
            return Integer.parseInt(valorNumerico);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Código de candidato inválido: " + codigo, e);
        }
    }
}