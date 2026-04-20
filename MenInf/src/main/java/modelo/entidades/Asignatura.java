package modelo.entidades;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.Serializable;

public class Asignatura implements Serializable {
    private int cod;
    private String nombre;
    private String abreviatura;
    private int cuatri;
    private String menciones = "";
    private String cursoAcademico = "";
    private String profesores = "";
    private String requisitos = "";
    private String lenguajes = "";

    public Asignatura() {}

    // Getters y Setters
    public int getCod() { return cod; }
    public void setCod(int cod) { this.cod = cod; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getAbreviatura() { return abreviatura; }
    public void setAbreviatura(String abreviatura) { this.abreviatura = abreviatura; }
    public int getCuatri() { return cuatri; }
    public void setCuatri(int cuatri) { this.cuatri = cuatri; }
    public String getMenciones() { return menciones; }
    public void setMenciones(String menciones) { this.menciones = menciones; }
    public String getCursoAcademico() { return cursoAcademico; }
    public void setCursoAcademico(String cursoAcademico) { this.cursoAcademico = cursoAcademico; }
    public String getProfesores() { return profesores; }
    public void setProfesores(String profesores) { this.profesores = profesores; }
    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
    public String getLenguajes() { return lenguajes; }
    public void setLenguajes(String lenguajes) { this.lenguajes = lenguajes; }

    /**
     * Obtiene todas las asignaturas de la base de datos.
     */
    public static java.util.List<Asignatura> listarTodas() {
        java.util.List<Asignatura> lista = new java.util.ArrayList<>();
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Asignatura ORDER BY nombre ASC");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Asignatura asig = new Asignatura();
                asig.setCod(rs.getInt("cod"));
                asig.setNombre(rs.getString("nombre"));
                asig.setAbreviatura(rs.getString("abreviatura"));
                asig.setCuatri(rs.getInt("cuatri"));
                lista.add(asig);
            }

            rs.close(); ps.close(); conn.close();
        } catch (Exception e) {
            System.err.println("Error en listarTodas: " + e.getMessage());
        }
        return lista;
    }

    public static Asignatura obtenerPorId(int cod) {
        Asignatura asig = null;
        System.out.println("Buscando asignatura con COD: " + cod); // LOG PARA CONSOLA
        
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            // CONSULTA SIMPLIFICADA PARA EVITAR ERRORES DE AGRUPACIÓN
            String sql = "SELECT * FROM Asignatura WHERE cod = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cod);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                asig = new Asignatura();
                asig.setCod(rs.getInt("cod"));
                asig.setNombre(rs.getString("nombre"));
                asig.setAbreviatura(rs.getString("abreviatura"));
                asig.setCuatri(rs.getInt("cuatri"));
                
                System.out.println("Asignatura encontrada: " + asig.getNombre());
                
                // Intentar cargar menciones por separado para no romper la consulta principal
                cargarDatosAdicionales(conn, asig);
            } else {
                System.out.println("No se encontró ninguna fila en la BD para el COD: " + cod);
            }

            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO en Asignatura.java: " + e.getMessage());
            e.printStackTrace();
        }
        return asig;
    }

    private static void cargarDatosAdicionales(Connection conn, Asignatura asig) {
        try {
            // Menciones
            PreparedStatement psM = conn.prepareStatement("SELECT mencion, cursoAcademico FROM AsignaturasMencion WHERE codA = ?");
            psM.setInt(1, asig.getCod());
            ResultSet rsM = psM.executeQuery();
            if(rsM.next()){
                asig.setMenciones(rsM.getString("mencion"));
                asig.setCursoAcademico(rsM.getString("cursoAcademico"));
            }
            rsM.close(); psM.close();

            // Profesores
            PreparedStatement psP = conn.prepareStatement("SELECT p.nombre FROM Profesor p JOIN ProfesorAsignatura pa ON p.correo = pa.correoP WHERE pa.codA = ?");
            psP.setInt(1, asig.getCod());
            ResultSet rsP = psP.executeQuery();
            StringBuilder profes = new StringBuilder();
            while(rsP.next()){
                if(profes.length() > 0) profes.append(", ");
                profes.append(rsP.getString("nombre"));
            }
            asig.setProfesores(profes.toString());
            rsP.close(); psP.close();
            
            // ... Podríamos añadir más, pero esto es lo básico para que no falle
        } catch (Exception e) {
            System.err.println("Error al cargar datos adicionales (ignorable): " + e.getMessage());
        }
    }
}
