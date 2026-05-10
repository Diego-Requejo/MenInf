package modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * DAO responsable de las operaciones de escritura sobre Usuario.
 */
public class UsuarioDAO {

    private Connection getConnection() throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        return ds.getConnection();
    }

    /**
     * Actualiza la mención elegida por el usuario en la BD.
     * Solo se llama desde ActualizarPerfil, cuando el usuario
     * elige la mención manualmente desde la página de progreso.
     */
    public void actualizarMencion(String nomU, String mencion) throws Exception {
        Connection conn = getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Usuario SET mencion = ? WHERE nombre = ?"
            );
            ps.setString(1, mencion);
            ps.setString(2, nomU);
            ps.executeUpdate();
            ps.close();
        } finally {
            conn.close();
        }
    }

    /**
     * Actualiza la biografía y la mención del usuario.
     * Llamado desde ActualizarPerfil.
     */
    public void actualizarPerfil(String nomU, String biografia,
                                  String mencion) throws Exception {
        Connection conn = getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE Usuario SET biografia = ?, mencion = ? WHERE nombre = ?"
            );
            ps.setString(1, biografia);
            ps.setString(2, mencion);
            ps.setString(3, nomU);
            ps.executeUpdate();
            ps.close();
        } finally {
            conn.close();
        }
    }
}
