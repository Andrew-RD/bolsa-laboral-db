package Datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class UbicacionDAO {

    private static final String SELECT_PROVINCIAS =
            "SELECT id_provincia, nombre FROM provincias ORDER BY nombre";

    private static final String SELECT_MUNICIPIOS =
            "SELECT id_municipio, nombre, id_provincia FROM municipio ORDER BY id_provincia, nombre";

    public LinkedHashMap<String, ArrayList<String>> listarMunicipiosPorProvincia() {
        LinkedHashMap<Integer, String> provinciasPorId = new LinkedHashMap<>();
        LinkedHashMap<String, ArrayList<String>> resultado = new LinkedHashMap<>();

        try (Connection con = Conexion.obtenerConexion()) {

            try (PreparedStatement ps = con.prepareStatement(SELECT_PROVINCIAS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    provinciasPorId.put(rs.getInt("id_provincia"), nombre);
                    resultado.put(nombre, new ArrayList<>());
                }
            }

            try (PreparedStatement ps = con.prepareStatement(SELECT_MUNICIPIOS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String provincia = provinciasPorId.get(rs.getInt("id_provincia"));
                    if (provincia != null) {
                        resultado.get(provincia).add(rs.getString("nombre"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo el catálogo geográfico desde la base de datos", e);
        }
        return resultado;
    }
}