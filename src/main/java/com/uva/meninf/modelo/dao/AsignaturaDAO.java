package com.uva.meninf.modelo.dao;

import com.uva.meninf.modelo.entidades.Asignatura;
import com.uva.meninf.modelo.entidades.Lenguaje;
import com.uva.meninf.modelo.entidades.Mencion;
import com.uva.meninf.modelo.entidades.Profesor;
import com.uva.meninf.modelo.enums.MencionTipo;
import com.uva.meninf.modelo.relaciones.AsignaturaMencion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * DAO responsable de todas las consultas sobre Asignatura.
 */
public class AsignaturaDAO {

    private Connection getConnection() throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        return ds.getConnection();
    }

    /**
     * Devuelve todas las asignaturas de 3º y 4º con sus menciones,
     * lenguajes y prerrequisitos.
     */
    public List<Asignatura> listarTerceroYCuarto() throws Exception {
        List<Asignatura> lista = new ArrayList<>();
        Connection conn = getConnection();

        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT DISTINCT a.cod, a.nombre, a.abreviatura, a.cuatri " +
                "FROM Asignatura a " +
                "JOIN AsignaturasMencion am ON a.cod = am.codA " +
                "WHERE am.cursoAcademico IN (3, 4) " +
                "ORDER BY a.nombre"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Asignatura asig = new Asignatura();
                asig.setCod(rs.getInt("cod"));
                asig.setNombre(rs.getString("nombre"));
                asig.setAbreviatura(rs.getString("abreviatura"));
                asig.setCuatri(rs.getInt("cuatri"));
                lista.add(asig);
            }
            rs.close();
            ps.close();

            for (Asignatura asig : lista) {
                cargarMenciones(conn, asig);
                cargarLenguajes(conn, asig);
                cargarPrerrequisitos(conn, asig);
            }
        } finally {
            conn.close();
        }

        return lista;
    }

    public List<Asignatura> listarTodas() throws Exception {
        List<Asignatura> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Asignatura ORDER BY nombre ASC");
            ResultSet rs = ps.executeQuery();     

            while (rs.next()) {
                Asignatura asig = new Asignatura();
                asig.setCod(rs.getInt("cod"));
                asig.setNombre(rs.getString("nombre"));
                asig.setAbreviatura(rs.getString("abreviatura"));
                asig.setCuatri(rs.getInt("cuatri"));            
                
                cargarMenciones(conn, asig); 
                lista.add(asig);
            }
            rs.close(); 
            ps.close();
        } finally {
            if (conn != null) conn.close();
        }
        return lista;
    }

    public Asignatura obtenerPorId(int cod) throws Exception {
        Asignatura asig = null;
        Connection conn = null;
        try {
            conn = getConnection();
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
                
                cargarMenciones(conn, asig);
                cargarLenguajes(conn, asig);
                cargarPrerrequisitos(conn, asig);
                cargarProfesores(conn, asig);
            }
            rs.close();
            ps.close();
        } finally {
            if (conn != null) conn.close();
        }
        return asig;
    }

    private void cargarMenciones(Connection conn, Asignatura asig) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT mencion, cursoAcademico, obligatoriedad " +
            "FROM AsignaturasMencion WHERE codA = ?"
        );
        ps.setInt(1, asig.getCod());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            try {
                MencionTipo tipo = MencionTipo.valueOf(rs.getString("mencion"));
                Mencion m = new Mencion(tipo);
                AsignaturaMencion am = new AsignaturaMencion();
                am.setMencion(m);
                am.setCursoAcademico(rs.getInt("cursoAcademico"));
                am.setObligatoriedad(rs.getBoolean("obligatoriedad"));
                asig.addMencion(am);
            } catch (IllegalArgumentException ignored) {}
        }
        rs.close();
        ps.close();
    }

    private void cargarLenguajes(Connection conn, Asignatura asig) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT l.nombre FROM Lenguaje l " +
            "JOIN LenguajesAsignatura la ON l.nombre = la.nomL WHERE la.codA = ?"
        );
        ps.setInt(1, asig.getCod());
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            asig.addLenguaje(new Lenguaje(rs.getString("nombre")));
        rs.close();
        ps.close();
    }

    private void cargarPrerrequisitos(Connection conn, Asignatura asig) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT a2.cod, a2.nombre, a2.abreviatura "+
            "FROM Asignatura a2 JOIN AsignaturaPrerrequisito ap ON a2.cod = ap.codAPre " +
            "WHERE ap.codA = ?"
        );
        ps.setInt(1, asig.getCod());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Asignatura req = new Asignatura();
            req.setCod(rs.getInt("cod"));
            req.setNombre(rs.getString("nombre"));
            req.setAbreviatura(rs.getString("abreviatura"));
            if (req.getCod() != asig.getCod()) 
                asig.addRequisito(req);
        }
        rs.close();
        ps.close();
    }

    private void cargarProfesores(Connection conn, Asignatura asig) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT p.nombre, p.correo FROM Profesor p " +
            "JOIN ProfesorAsignatura pa ON p.correo = pa.correoP WHERE pa.codA = ?"
        );
        ps.setInt(1, asig.getCod());
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            Profesor prof = new Profesor(rs.getString("nombre"), rs.getString("correo"));
            asig.addProfesor(prof);
        } 
        rs.close(); 
        ps.close();
    }
}
