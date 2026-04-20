package modelo.entidades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import modelo.enums.EstadoAsignatura;
import modelo.relaciones.Cursada;

public class Usuario {
    private String nombre;
    private String password;
    private String biografia;
    private String mencion;
    private boolean tfg;
    private boolean practicas;

    private ArrayList<Cursada> cursadas = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
    }

    //##########################################
    //########### GETTERS Y SETTERS ############
    //##########################################

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getMencion() { return mencion; }
    public void setMencion(String mencion) { this.mencion = mencion; }

    public boolean isTfg() { return tfg; }
    public void setTfg(boolean tfg) { this.tfg = tfg; }

    public boolean isPracticas() { return practicas; }
    public void setPracticas(boolean practicas) { this.practicas = practicas; }

    public ArrayList<Cursada> getCursadas() { return cursadas; }
    public void setCursadas(ArrayList<Cursada> cursadas) { this.cursadas = cursadas; }

    //######################################################################
    //# Métodos estáticos equivalentes para iteración con la base de datos #
    //######################################################################
    public static boolean existeUsuario(String nombre) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Usuario WHERE nombre = ?"
            );
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            ps.close();
            conn.close();

            return count > 0;
        } catch (Exception e) {
            System.err.println("Error en existeUsuario: " + e.getMessage());
            return false;
        }
    }

    public static void registrar(String nombre, String password) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Usuario (nombre, contseña, biografia, mencion, tfg, practicas) " +
                "VALUES (?, ?, NULL, 'PreMención', FALSE, FALSE)"
            );
            ps.setString(1, nombre);
            ps.setString(2, password);
            ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error en registrar: " + e.getMessage());
        }
    }

    public static boolean login(String nombre, String password) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Usuario WHERE nombre = ? AND contseña = ?"
            );
            ps.setString(1, nombre);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            ps.close();
            conn.close();

            return count > 0;
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            return false;
        }
    }

    public static Usuario obtenerPerfil(String nombre) {
        Usuario usuario = null;
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Usuario WHERE nombre = ?" );
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                usuario = new Usuario();
                usuario.setNombre(rs.getString("nombre"));
                usuario.setPassword(rs.getString("contseña"));
                usuario.setBiografia(rs.getString("biografia"));
                usuario.setMencion(rs.getString("mencion"));
                usuario.setTfg(rs.getBoolean("tfg"));
                usuario.setPracticas(rs.getBoolean("practicas"));
            }

            PreparedStatement psCursadas = conn.prepareStatement( "SELECT * FROM Cursada WHERE nombre = ?" );
            psCursadas.setString(1, nombre);
            ResultSet rsCursadas = psCursadas.executeQuery();

            while (rsCursadas.next()) {
                int codAsig = rsCursadas.getInt("codAsignatura");
                Asignatura asig = Asignatura.obtenerPorId(codAsig);
                
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
            conn.close();
        } catch (Exception e) {
            System.err.println("Error en obtenerPerfil: " + e.getMessage());
        }
        return usuario;
    }

    private void añadirCursada(Cursada cursada) {
        this.cursadas.add(cursada);
    }
}