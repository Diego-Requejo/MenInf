package com.uva.meninf.modelo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Year;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


/**
 * DAO responsable exclusivamente de persistir los datos
 * del formulario de afinidad académica en la base de datos.
 */
public class FormularioDAO {

    private Connection getConnection() throws Exception {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");
        return ds.getConnection();
    }

    public void guardarFormulario(String nomU,
                                   String[] favoritas, String[] odiadas,
                                   String[] superadas, String[] suspensas,
                                   String python, String mips,
                                   String nivelJava, String c) throws Exception {

        Connection conn = getConnection();
        int year = Year.now().getValue();

        try {
            // Primero eliminar todas las cursadas anteriores del usuario
            eliminarCursadasAnteriores(conn, nomU);
            // Luego insertar las nuevas
        if (superadas != null)
            for (String cod : superadas)
                insertarCursada(conn, Integer.parseInt(cod), nomU, "superada", year,
                               contiene(favoritas, cod), contiene(odiadas, cod));

        if (suspensas != null)
            for (String cod : suspensas)
                insertarCursada(conn, Integer.parseInt(cod), nomU, "suspensa", year,
                               contiene(favoritas, cod), contiene(odiadas, cod));

        // Las marcadas como favoritas u odiadas pero no superadas ni suspensas
        if (favoritas != null)
            for (String cod : favoritas)
                if (!contiene(superadas, cod) && !contiene(suspensas, cod))
                    insertarCursada(conn, Integer.parseInt(cod), nomU, null, year,
                                   true, contiene(odiadas, cod));

        if (odiadas != null)
            for (String cod : odiadas)
                if (!contiene(superadas, cod) && !contiene(suspensas, cod)
                        && !contiene(favoritas, cod))
                    insertarCursada(conn, Integer.parseInt(cod), nomU, null, year,
                                   false, true);

            guardarPreferenciaLenguaje(conn, nomU, "Python",           python);
            guardarPreferenciaLenguaje(conn, nomU, "Ensamblador MIPS", mips);
            guardarPreferenciaLenguaje(conn, nomU, "Java",             nivelJava);
            guardarPreferenciaLenguaje(conn, nomU, "C",                c);

        } finally {
            conn.close();
        }
    }

    private void insertarCursada(Connection conn, int codA, String nomU,
                                  String estado, int anio,
                                  boolean favorita, boolean odiada) throws Exception {
        String sql = "INSERT INTO Cursada (codA, nomU, estado, año, favorita, odiada) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE estado=?, año=?, favorita=?, odiada=?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, codA);      ps.setString(2, nomU);
        ps.setString(3, estado); ps.setInt(4, anio);
        ps.setBoolean(5, favorita); ps.setBoolean(6, odiada);
        ps.setString(7, estado); ps.setInt(8, anio);
        ps.setBoolean(9, favorita); ps.setBoolean(10, odiada);
        ps.executeUpdate();
        ps.close();
    }

    private void guardarPreferenciaLenguaje(Connection conn, String nomU,
                                             String nomL, String valor) throws Exception {
        if (valor == null) return;
        int nota = Integer.parseInt(valor);

        String sql = "INSERT INTO PreferenciaLenguaje (nomU, nomL, nota) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nota=?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, nomU); ps.setString(2, nomL);
        ps.setInt(3, nota);    ps.setInt(4, nota);
        ps.executeUpdate();
        ps.close();
    }

    private boolean contiene(String[] array, String valor) {
        if (array == null) return false;
        for (String s : array) if (s.equals(valor)) return true;
        return false;
    }
    
    private void eliminarCursadasAnteriores(Connection conn, String nomU) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM Cursada WHERE nomU = ?"
        );
        ps.setString(1, nomU);
        ps.executeUpdate();
        ps.close();
    }
}
