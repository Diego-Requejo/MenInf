package modelo.entidades;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class Periodo implements Serializable {

    private String idPeriodo;
    private String grupo;
    private int dia;
    private int horaIni;
    private int horaFin;
    private int codA;

    public Periodo() {}

    public String getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(String idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getHoraIni() {
        return horaIni;
    }

    public void setHoraIni(int horaIni) {
        this.horaIni = horaIni;
    }

    public int getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(int horaFin) {
        this.horaFin = horaFin;
    }

    public int getCodA() {
        return codA;
    }

    public void setCodA(int codA) {
        this.codA = codA;
    }
    
    public static ArrayList<Periodo> obtenerPorAsignatura(int codA) {

        ArrayList<Periodo> lista = new ArrayList<>();

        try {

            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
            Connection conn = ds.getConnection();

            String sql = "SELECT * FROM Periodo WHERE codA = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, codA);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {

                Periodo p = new Periodo();

                p.setIdPeriodo(rs.getString("idPeriodo"));
                p.setGrupo(rs.getString("grupo"));
                p.setDia(rs.getInt("dia"));
                p.setHoraIni(rs.getInt("horaIni"));
                p.setHoraFin(rs.getInt("horaFin"));
                p.setCodA(rs.getInt("codA"));

                lista.add(p);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

        return lista;
    }
}