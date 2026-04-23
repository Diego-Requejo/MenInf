package modelo.entidades;

import modelo.enums.MencionTipo;
import modelo.relaciones.AsignaturaMencion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.Serializable;

public class Asignatura implements Serializable {
    private int cod;
    private String nombre;
    private String abreviatura;
    private int cuatri;
    private ArrayList<AsignaturaMencion> menciones;
    private ArrayList<Profesor> profesores;
    private ArrayList<Asignatura> requisitos;
    private ArrayList<Lenguaje> lenguajes;

    public Asignatura() {
        profesores = new ArrayList<>();
        requisitos = new ArrayList<>();
        lenguajes = new ArrayList<>();
        menciones = new ArrayList<>();
    }

    // Getters y Setters
    public int getCod() { return cod; }
    public void setCod(int cod) { this.cod = cod; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getAbreviatura() { return abreviatura; }
    public void setAbreviatura(String abreviatura) { this.abreviatura = abreviatura; }
    public int getCuatri() { return cuatri; }
    public void setCuatri(int cuatri) { this.cuatri = cuatri; }
    public ArrayList<AsignaturaMencion> getMenciones() { return menciones; }
    public void setMenciones(ArrayList<AsignaturaMencion> menciones) { this.menciones = menciones; }
    public void addMencion(AsignaturaMencion mencion) { this.menciones.add(mencion); }
    public ArrayList<Profesor> getProfesores() { return profesores; }
    public void setProfesores(ArrayList<Profesor> profesores) { this.profesores = profesores; }
    public void addProfesor(Profesor profesor) { this.profesores.add(profesor); }
    public ArrayList<Asignatura> getRequisitos() { return requisitos; }
    public void setRequisitos(ArrayList<Asignatura> requisitos) { this.requisitos = requisitos; }
    public void addRequisito(Asignatura requisito) { this.requisitos.add(requisito); }
    public ArrayList<Lenguaje> getLenguajes() { return lenguajes; }
    public void setLenguajes(ArrayList<Lenguaje> lenguajes) { this.lenguajes = lenguajes; }
    public void addLenguaje(Lenguaje lenguaje) { this.lenguajes.add(lenguaje); }

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
                Mencion m = new Mencion(MencionTipo.valueOf(rsM.getString("mencion")));
                AsignaturaMencion am = new AsignaturaMencion();
                am.setMencion(m);
                am.setCursoAcademico(rsM.getInt("cursoAcademico"));
                asig.addMencion(am);
            }
            rsM.close(); psM.close();

            // Profesores
            PreparedStatement psP = conn.prepareStatement("SELECT p.nombre, p.correo FROM Profesor p JOIN ProfesorAsignatura pa ON p.correo = pa.correoP WHERE pa.codA = ?");
            psP.setInt(1, asig.getCod());
            ResultSet rsP = psP.executeQuery();

            while(rsP.next()){
                Profesor prof = new Profesor(rsP.getString("nombre"), rsP.getString("correo"));
                asig.addProfesor(prof);
            } 
            rsP.close(); psP.close();

            // Lenguajes
            PreparedStatement psL = conn.prepareStatement("SELECT l.nombre FROM Lenguaje l JOIN LenguajesAsignatura la ON l.nombre = la.nomL WHERE la.codA = ?");
            psL.setInt(1, asig.getCod());
            ResultSet rsL = psL.executeQuery();

            while(rsL.next()){
                Lenguaje lenguaje = new Lenguaje(rsL.getString("nombre"));
                asig.addLenguaje(lenguaje);
            }
            rsL.close(); psL.close();

            PreparedStatement psReq = conn.prepareStatement("SELECT a2.cod, a2.nombre, a2.abreviatura FROM Asignatura a2 JOIN AsignaturaPrerrequisito ap ON a2.cod = ap.codAPre WHERE ap.codA = ?");
            psReq.setInt(1, asig.getCod());
            ResultSet rsReq = psReq.executeQuery();

            while(rsReq.next()){
                Asignatura req = new Asignatura();
                req.setCod(rsReq.getInt("cod"));
                req.setNombre(rsReq.getString("nombre"));
                req.setAbreviatura(rsReq.getString("abreviatura"));
                asig.addRequisito(req);
            }
            rsReq.close(); psReq.close();
           
            // ... Podríamos añadir más, pero esto es lo básico para que no falle
        } catch (Exception e) {
            System.err.println("Error al cargar datos adicionales (ignorable): " + e.getMessage());
        }
    }
}
