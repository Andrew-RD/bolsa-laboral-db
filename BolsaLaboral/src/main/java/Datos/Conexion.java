package Datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433"
                    + ";databaseName=BolsaLaboral"
                    + ";encrypt=true"
                    + ";trustServerCertificate=true";

    public static Connection obtenerConexion() throws SQLException {
        String usuario = System.getenv("DB_USER");
        String contrasena = System.getenv("DB_PASSWORD");

        return DriverManager.getConnection(URL, usuario, contrasena);
    }
}