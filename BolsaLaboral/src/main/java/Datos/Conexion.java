package Datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String SERVIDOR = "localhost";
    private static final String BASE_DATOS = "BolsaLaboral";

    private static final String URL = "jdbc:sqlserver://" + SERVIDOR
            + ";databaseName=" + BASE_DATOS
            + ";integratedSecurity=true"
            + ";encrypt=true;trustServerCertificate=true";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}