package Datos;

import exception.NotRemovableException;
import logico.CentroEmpleador;
import logico.OfertaLaboral;
import logico.TipoCandidato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class OfertaLaboralDAO {

    private static final String PREFIJO_CODIGO = "OFR-";
    private static final String PREFIJO_CODIGO_CENTRO = "CEN-";

    private static final String SELECT_TODOS =
            "SELECT o.id_oferta, o.puesto, o.descripcion, o.modalidad, o.jornada, o.estado, " +
                    "o.salario, o.experienciaMinima, o.vacantesTotales, o.ofreceReubicacion, " +
                    "o.obligatorioMayorDeEdad, o.obligatorioLicencia, o.nivelAcademico, " +
                    "o.tipoCandidatoRequerido, o.porcentajeMinimo, " +
                    "al.nombre AS area, " +
                    "ce.id_centroEmpleador, ce.rnc, ce.nombre AS centroNombre, " +
                    "ce.telefono AS centroTelefono, ce.correo AS centroCorreo, " +
                    "s.nombre AS sector, p.nombre AS provincia, m.nombre AS municipio " +
                    "FROM ofertas o " +
                    "JOIN areasLaborales al ON al.id_areaLaboral = o.id_areaLaboral " +
                    "JOIN centrosEmpleadores ce ON ce.id_centroEmpleador = o.id_centroEmpleador " +
                    "JOIN sectores s ON s.id_sector = ce.id_sector " +
                    "JOIN municipio m ON m.id_municipio = ce.id_municipio " +
                    "JOIN provincias p ON p.id_provincia = m.id_provincia " +
                    "ORDER BY o.fechaPublicacion DESC, o.id_oferta DESC";

    private static final String SELECT_IDIOMAS_TODOS =
            "SELECT oi.id_oferta, i.nombre " +
                    "FROM ofertas_idiomas oi " +
                    "JOIN idiomas i ON i.id_idioma = oi.id_idioma";

    private static final String SELECT_REQUISITOS_TODOS =
            "SELECT orr.id_oferta, r.nombre " +
                    "FROM ofertas_requerimientos orr " +
                    "JOIN requerimientos r ON r.id_requerimiento = orr.id_requerimiento";

    private static final String SELECT_OCUPADAS_TODOS =
            "SELECT sol.id_oferta, COUNT(*) AS ocupadas " +
                    "FROM Contrataciones c " +
                    "JOIN solicitudes sol ON sol.id_solicitud = c.id_solicitud " +
                    "GROUP BY sol.id_oferta";

    private static final String INSERT =
            "INSERT INTO ofertas " +
                    "(puesto, descripcion, modalidad, jornada, estado, salario, experienciaMinima, " +
                    "vacantesTotales, ofreceReubicacion, obligatorioMayorDeEdad, obligatorioLicencia, " +
                    "nivelAcademico, tipoCandidatoRequerido, porcentajeMinimo, id_areaLaboral, " +
                    "id_centroEmpleador) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE ofertas SET " +
                    "puesto = ?, descripcion = ?, modalidad = ?, jornada = ?, estado = ?, " +
                    "salario = ?, experienciaMinima = ?, vacantesTotales = ?, ofreceReubicacion = ?, " +
                    "obligatorioMayorDeEdad = ?, obligatorioLicencia = ?, nivelAcademico = ?, " +
                    "tipoCandidatoRequerido = ?, porcentajeMinimo = ?, id_areaLaboral = ?, " +
                    "id_centroEmpleador = ? " +
                    "WHERE id_oferta = ?";

    private static final String DELETE_IDIOMAS_OFERTA =
            "DELETE FROM ofertas_idiomas WHERE id_oferta = ?";

    private static final String INSERT_IDIOMA_OFERTA =
            "INSERT INTO ofertas_idiomas (id_oferta, id_idioma) VALUES (?, ?)";

    private static final String DELETE_REQUISITOS_OFERTA =
            "DELETE FROM ofertas_requerimientos WHERE id_oferta = ?";

    private static final String INSERT_REQUISITO_OFERTA =
            "INSERT INTO ofertas_requerimientos (id_oferta, id_requerimiento) VALUES (?, ?)";

    private static final String SELECT_ID_AREA_LABORAL =
            "SELECT id_areaLaboral FROM areasLaborales WHERE nombre = ?";

    private static final String SELECT_ID_IDIOMA =
            "SELECT id_idioma FROM idiomas WHERE nombre = ?";

    private static final String COUNT_SOLICITUDES =
            "SELECT COUNT(*) FROM solicitudes WHERE id_oferta = ?";

    private static final String DELETE_CONTRATACIONES_DE_OFERTA =
            "DELETE FROM Contrataciones WHERE id_solicitud IN " +
                    "(SELECT id_solicitud FROM solicitudes WHERE id_oferta = ?)";

    private static final String DELETE_SOLICITUDES_DE_OFERTA =
            "DELETE FROM solicitudes WHERE id_oferta = ?";

    private static final String DELETE_OFERTA =
            "DELETE FROM ofertas WHERE id_oferta = ?";

    public ArrayList<OfertaLaboral> listarTodos() {
        ArrayList<OfertaLaboral> resultado = new ArrayList<>();

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_TODOS);
             ResultSet rs = ps.executeQuery()) {

            LinkedHashMap<Integer, ArrayList<String>> idiomasPorOferta = cargarIdiomasPorOferta(con);
            LinkedHashMap<Integer, ArrayList<String>> requisitosPorOferta = cargarRequisitosPorOferta(con);
            LinkedHashMap<Integer, Integer> ocupadasPorOferta = cargarOcupadasPorOferta(con);

            while (rs.next()) {
                resultado.add(mapearOferta(rs, idiomasPorOferta, requisitosPorOferta, ocupadasPorOferta));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo ofertas laborales desde la base de datos", e);
        }

        return resultado;
    }

    public void agregar(OfertaLaboral oferta) {
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idAreaLaboral = buscarIdAreaLaboral(con, oferta.getArea());
                int idCentro = extraerIdCentro(oferta.getOfertador().getCodigo());

                int idGenerado;
                try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    llenarParametros(ps, oferta, idAreaLaboral, idCentro);
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException("No se obtuvo el id generado para la oferta.");
                        }
                        idGenerado = claves.getInt(1);
                    }
                }

                insertarIdiomas(con, idGenerado, oferta.getIdiomasRequeridas());
                insertarRequisitos(con, idGenerado, oferta.getTipoCandidatoRequerido(), oferta.getRequisitos());

                con.commit();
                oferta.setCodigo(crearCodigo(idGenerado));
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error agregando la oferta laboral", e);
        }
    }

    public void modificar(OfertaLaboral oferta) {
        int idOferta = extraerIdDelCodigo(oferta.getCodigo());

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idAreaLaboral = buscarIdAreaLaboral(con, oferta.getArea());
                int idCentro = extraerIdCentro(oferta.getOfertador().getCodigo());

                try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                    int indice = llenarParametros(ps, oferta, idAreaLaboral, idCentro);
                    ps.setInt(indice, idOferta);

                    int filasModificadas = ps.executeUpdate();
                    if (filasModificadas == 0) {
                        throw new SQLException("No existe una oferta con id_oferta = " + idOferta + ".");
                    }
                }

                reemplazarIdiomas(con, idOferta, oferta.getIdiomasRequeridas());
                reemplazarRequisitos(con, idOferta, oferta.getTipoCandidatoRequerido(), oferta.getRequisitos());

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error modificando la oferta laboral", e);
        }
    }

    public int contarSolicitudes(OfertaLaboral oferta) {
        int idOferta = extraerIdDelCodigo(oferta.getCodigo());

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(COUNT_SOLICITUDES)) {

            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error contando las solicitudes de la oferta laboral", e);
        }
        return 0;
    }

    public void eliminar(OfertaLaboral oferta) throws NotRemovableException {
        int idOferta = extraerIdDelCodigo(oferta.getCodigo());

        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                eliminarSolicitudesYVinculos(con, idOferta);

                try (PreparedStatement ps = con.prepareStatement(DELETE_IDIOMAS_OFERTA)) {
                    ps.setInt(1, idOferta);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(DELETE_REQUISITOS_OFERTA)) {
                    ps.setInt(1, idOferta);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(DELETE_OFERTA)) {
                    ps.setInt(1, idOferta);
                    int filas = ps.executeUpdate();
                    if (filas == 0) {
                        throw new SQLException("No existe una oferta con id_oferta = " + idOferta + ".");
                    }
                }

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                if (esViolacionDeIntegridadReferencial(e)) {
                    throw new NotRemovableException(
                            "La oferta laboral no puede ser eliminada porque tiene información vinculada que no pudo eliminarse automáticamente.",
                            e);
                }
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando la oferta laboral", e);
        }
    }

    private void eliminarSolicitudesYVinculos(Connection con, int idOferta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_CONTRATACIONES_DE_OFERTA)) {
            ps.setInt(1, idOferta);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(DELETE_SOLICITUDES_DE_OFERTA)) {
            ps.setInt(1, idOferta);
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

    private int llenarParametros(
            PreparedStatement ps,
            OfertaLaboral oferta,
            int idAreaLaboral,
            int idCentro) throws SQLException {

        int i = 1;
        ps.setString(i++, oferta.getPuesto());
        ps.setString(i++, oferta.getDescripcion());
        ps.setString(i++, oferta.getModalidad());
        ps.setString(i++, oferta.getJornada());
        ps.setString(i++, oferta.getEstado());
        ps.setFloat(i++, oferta.getSalario());
        ps.setInt(i++, oferta.getExperienciaMinima());
        ps.setInt(i++, oferta.getVacantesTotales());
        ps.setBoolean(i++, oferta.isOfreceReubicacion());
        ps.setBoolean(i++, oferta.isObligatorioMayorDeEdad());
        ps.setBoolean(i++, oferta.isobligatorioLicencia());
        ps.setString(i++, oferta.getNivelAcademico());
        ps.setString(i++, oferta.getTipoCandidatoRequerido().name());
        ps.setInt(i++, oferta.getPorcentajeMinimo());
        ps.setInt(i++, idAreaLaboral);
        ps.setInt(i++, idCentro);
        return i;
    }

    private void insertarIdiomas(Connection con, int idOferta, ArrayList<String> idiomas) throws SQLException {
        if (idiomas == null || idiomas.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(INSERT_IDIOMA_OFERTA)) {
            for (String nombreIdioma : idiomas) {
                int idIdioma = buscarIdIdioma(con, nombreIdioma);
                ps.setInt(1, idOferta);
                ps.setInt(2, idIdioma);
                ps.executeUpdate();
            }
        }
    }

    private void reemplazarIdiomas(Connection con, int idOferta, ArrayList<String> idiomas) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_IDIOMAS_OFERTA)) {
            ps.setInt(1, idOferta);
            ps.executeUpdate();
        }
        insertarIdiomas(con, idOferta, idiomas);
    }

    private void insertarRequisitos(
            Connection con,
            int idOferta,
            TipoCandidato tipoCandidatoRequerido,
            ArrayList<String> requisitos) throws SQLException {

        if (requisitos == null || requisitos.isEmpty()) {
            return;
        }
        String tablaSubtipo = tablaSubtipoDeRequisitos(tipoCandidatoRequerido);
        try (PreparedStatement ps = con.prepareStatement(INSERT_REQUISITO_OFERTA)) {
            for (String nombreRequisito : requisitos) {
                int idRequerimiento = buscarIdRequerimiento(con, tablaSubtipo, nombreRequisito);
                ps.setInt(1, idOferta);
                ps.setInt(2, idRequerimiento);
                ps.executeUpdate();
            }
        }
    }

    private void reemplazarRequisitos(
            Connection con,
            int idOferta,
            TipoCandidato tipoCandidatoRequerido,
            ArrayList<String> requisitos) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(DELETE_REQUISITOS_OFERTA)) {
            ps.setInt(1, idOferta);
            ps.executeUpdate();
        }
        insertarRequisitos(con, idOferta, tipoCandidatoRequerido, requisitos);
    }

    private String tablaSubtipoDeRequisitos(TipoCandidato tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de candidato requerido es obligatorio.");
        }
        switch (tipo) {
            case UNIVERSITARIO:
                return "carreras";
            case TECNICO:
                return "areasTecnicas";
            case OBRERO:
                return "habilidades";
            default:
                throw new IllegalArgumentException("Tipo de candidato no soportado: " + tipo);
        }
    }

    private LinkedHashMap<Integer, ArrayList<String>> cargarIdiomasPorOferta(Connection con) throws SQLException {
        LinkedHashMap<Integer, ArrayList<String>> resultado = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_IDIOMAS_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idOferta = rs.getInt("id_oferta");
                resultado.computeIfAbsent(idOferta, k -> new ArrayList<>()).add(rs.getString("nombre"));
            }
        }
        return resultado;
    }

    private LinkedHashMap<Integer, ArrayList<String>> cargarRequisitosPorOferta(Connection con) throws SQLException {
        LinkedHashMap<Integer, ArrayList<String>> resultado = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_REQUISITOS_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idOferta = rs.getInt("id_oferta");
                resultado.computeIfAbsent(idOferta, k -> new ArrayList<>()).add(rs.getString("nombre"));
            }
        }
        return resultado;
    }

    private LinkedHashMap<Integer, Integer> cargarOcupadasPorOferta(Connection con) throws SQLException {
        LinkedHashMap<Integer, Integer> resultado = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_OCUPADAS_TODOS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.put(rs.getInt("id_oferta"), rs.getInt("ocupadas"));
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

    private OfertaLaboral mapearOferta(
            ResultSet rs,
            LinkedHashMap<Integer, ArrayList<String>> idiomasPorOferta,
            LinkedHashMap<Integer, ArrayList<String>> requisitosPorOferta,
            LinkedHashMap<Integer, Integer> ocupadasPorOferta) throws SQLException {

        int idOferta = rs.getInt("id_oferta");
        int idCentro = rs.getInt("id_centroEmpleador");

        CentroEmpleador ofertador = new CentroEmpleador(
                PREFIJO_CODIGO_CENTRO + idCentro,
                rs.getString("centroNombre"),
                rs.getString("sector"),
                rs.getString("provincia"),
                rs.getString("municipio"),
                rs.getString("centroTelefono"),
                rs.getString("centroCorreo"),
                rs.getString("rnc")
        );

        ArrayList<String> idiomas = idiomasPorOferta.getOrDefault(idOferta, new ArrayList<>());
        ArrayList<String> requisitos = requisitosPorOferta.getOrDefault(idOferta, new ArrayList<>());
        int ocupadas = ocupadasPorOferta.getOrDefault(idOferta, 0);

        OfertaLaboral oferta = new OfertaLaboral(
                crearCodigo(idOferta),
                rs.getString("puesto"),
                rs.getString("descripcion"),
                rs.getString("area"),
                rs.getString("modalidad"),
                rs.getString("jornada"),
                rs.getString("estado"),
                rs.getFloat("salario"),
                rs.getInt("experienciaMinima"),
                rs.getInt("vacantesTotales"),
                ofertador,
                rs.getBoolean("ofreceReubicacion"),
                rs.getBoolean("obligatorioMayorDeEdad"),
                rs.getBoolean("obligatorioLicencia"),
                rs.getString("nivelAcademico"),
                requisitos,
                idiomas,
                rs.getInt("porcentajeMinimo")
        );

        oferta.sincronizarVacantesOcupadas(ocupadas);

        return oferta;
    }

    private String crearCodigo(int idOferta) {
        return PREFIJO_CODIGO + idOferta;
    }

    private int extraerIdDelCodigo(String codigo) {
        return extraerId(codigo, PREFIJO_CODIGO, "La oferta laboral no tiene código.", "Código de oferta laboral inválido: ");
    }

    private int extraerIdCentro(String codigo) {
        return extraerId(codigo, PREFIJO_CODIGO_CENTRO, "El centro empleador de la oferta no tiene código.", "Código de centro empleador inválido: ");
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