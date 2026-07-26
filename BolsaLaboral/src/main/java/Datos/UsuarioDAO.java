package Datos;

import logico.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class UsuarioDAO {

    public ArrayList<Usuario> listarTodos() {
        ArrayList<Usuario> resultado = new ArrayList<>();
        String sql = "SELECT nombre_usuario, contrasena, tipo FROM usuarios";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String nombreUsuario = rs.getString("nombre_usuario");
                String contrasena = rs.getString("contrasena");
                String tipo = rs.getString("tipo");
                resultado.add(new Usuario(nombreUsuario, contrasena, tipo));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error leyendo usuarios desde la base de datos", e);
        }
        return resultado;
    }
}