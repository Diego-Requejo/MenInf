package com.uva.meninf.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.uva.meninf.modelo.entidades.Periodo;

public class PeriodoDAO {
    
    private Connection getConnection() throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        return ds.getConnection();
    }

    public ArrayList<Periodo> obtenerPorAsignatura(int codA) throws Exception {
        ArrayList<Periodo> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
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
        } finally {
            conn.close();
        }
        return lista;
    }

}
