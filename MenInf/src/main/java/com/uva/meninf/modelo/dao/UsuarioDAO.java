package com.uva.meninf.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.uva.meninf.modelo.dto.DatosProgreso;
import com.uva.meninf.modelo.entidades.Asignatura;
import com.uva.meninf.modelo.entidades.Usuario;
import com.uva.meninf.modelo.enums.EstadoAsignatura;
import com.uva.meninf.modelo.relaciones.Cursada;

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
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "UPDATE Usuario SET biografia = ?, mencion = ? WHERE nombre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, biografia);
            ps.setString(2, mencion);
            ps.setString(3, nomU);
            ps.executeUpdate();

            ps.close();
        } finally {
            conn.close();
        }
    }

    public boolean existeUsuario(String nombre) throws Exception {
        boolean existe = false;
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "SELECT COUNT(*) FROM Usuario WHERE nombre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            
            ResultSet rs = ps.executeQuery();
            rs.next();
            existe = rs.getInt(1) > 0;
            rs.close();
            ps.close();
        } finally{
            conn.close();
        }
        return existe;
    }

    public void registrar(String nombre, String password) throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "INSERT INTO Usuario (nombre, contraseña, biografia, mencion, tfg, practicas) " +
                         "VALUES (?, ?, NULL, 'PreMencion', FALSE, FALSE)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, password);
            ps.executeUpdate();
            ps.close();
        } finally{
            conn.close();
        }
    }

    public Usuario obtenerPerfil(String nombre) throws Exception {
        Usuario usuario = null;
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "SELECT * FROM Usuario WHERE nombre = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                usuario = new Usuario();
                usuario.setNombre(rs.getString("nombre"));
                usuario.setPassword(rs.getString("contraseña"));
                usuario.setBiografia(rs.getString("biografia"));
                usuario.setMencion(rs.getString("mencion"));
                usuario.setTfg(rs.getBoolean("tfg"));
                usuario.setPracticas(rs.getBoolean("practicas"));
            }

            String sqlCursadas = "SELECT * FROM Cursada WHERE nombre = ?";   
            PreparedStatement psCursadas = conn.prepareStatement( sqlCursadas);
            psCursadas.setString(1, nombre);
            ResultSet rsCursadas = psCursadas.executeQuery();

            while (rsCursadas.next()) {
                int codAsig = rsCursadas.getInt("codAsignatura");
                AsignaturaDAO asignaturaDAO = new AsignaturaDAO();
                Asignatura asig = asignaturaDAO.obtenerPorId(codAsig);
                
                if( asig != null ){
                    String estadoStr = rsCursadas.getString("estado");
                    EstadoAsignatura estado = EstadoAsignatura.valueOf(estadoStr);

                    Cursada cursada = new Cursada();
                    cursada.setUsuario(usuario);
                    cursada.setAsignatura(asig);
                    cursada.setEstado(estado);
                    cursada.setAño(rsCursadas.getInt("año"));
                    cursada.setFavorita(rsCursadas.getBoolean("favorita"));
                    cursada.setOdiada(rsCursadas.getBoolean("odiada"));

                    usuario.añadirCursada(cursada);
                }
            }
            rs.close();
            ps.close();
        } finally {
            conn.close();
        }
        return usuario;
    }

    public boolean login(String nombre, String password) throws Exception {
        boolean loginCorrecto = false;
        Connection conn = null;
        try {
            conn = getConnection();
            String sql = "SELECT COUNT(*) FROM Usuario WHERE nombre = ? AND contraseña = ?";    
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            rs.next();
            loginCorrecto = rs.getInt(1) > 0;
            rs.close();
            ps.close();

        } finally{
            conn.close();
        }
        return loginCorrecto;
    }

    public DatosProgreso obtenerDatosProgreso(String nomU) throws Exception {
    DatosProgreso datos = new DatosProgreso();
    Connection conn = getConnection();
    try {
        PreparedStatement psU = conn.prepareStatement(
            "SELECT biografia, mencion, tfg, practicas FROM Usuario WHERE nombre = ?"
        );
        psU.setString(1, nomU);
        ResultSet rsU = psU.executeQuery();
        if (rsU.next()) {
            String bio = rsU.getString("biografia");
            datos.biografia = bio != null ? bio : "";
            datos.mencion   = rsU.getString("mencion");
            datos.tfg       = rsU.getBoolean("tfg");
            datos.practicas = rsU.getBoolean("practicas");
        }
        rsU.close(); psU.close();

        PreparedStatement psC = conn.prepareStatement(
            "SELECT a.cod, a.nombre, c.estado, c.favorita, c.odiada " +
            "FROM Cursada c JOIN Asignatura a ON c.codA = a.cod " +
            "WHERE c.nomU = ? ORDER BY a.nombre"
        );
        psC.setString(1, nomU);
        ResultSet rsC = psC.executeQuery();
        while (rsC.next()) {
            int    cod    = rsC.getInt("cod");
            String nombre = rsC.getString("nombre");
            String estado = rsC.getString("estado");
            if ("superada".equals(estado))    datos.superadas.put(cod, nombre);
            if ("suspensa".equals(estado))    datos.suspensas.put(cod, nombre);
            if (rsC.getBoolean("favorita"))   datos.favoritas.put(cod, nombre);
            if (rsC.getBoolean("odiada"))     datos.odiadas.put(cod, nombre);
        }
        rsC.close(); psC.close();
    } finally {
        conn.close();
    }
    return datos;
}
}
