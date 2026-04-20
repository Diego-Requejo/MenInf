package modelo.relaciones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import modelo.entidades.Asignatura;
import modelo.entidades.Usuario;
import modelo.enums.EstadoAsignatura;

public class Cursada {

    private EstadoAsignatura estado;
    private int año;
    private boolean favorita;
    private boolean odiada;

    private Usuario usuario;
    private Asignatura asignatura;

    public Cursada() {}

    public Cursada(Usuario usuario, Asignatura asignatura, EstadoAsignatura estado, int año, boolean favorita, boolean odiada) {
        this.usuario = usuario;
        this.asignatura = asignatura;
        this.estado = estado;
        this.año = año;
        this.favorita = favorita;
        this.odiada = odiada;
    }

    public EstadoAsignatura getEstado() { return estado; }
    public void setEstado(EstadoAsignatura estado) { this.estado = estado; }    

    public int getAño() { return año; } 
    public void setAño(int año) { this.año = año; }

    public boolean isFavorita() { return favorita; }
    public void setFavorita(boolean favorita) { this.favorita = favorita; }

    public boolean isOdiada() { return odiada; }
    public void setOdiada(boolean odiada) { this.odiada = odiada; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Asignatura getAsignatura() { return asignatura; }
    public void setAsignatura(Asignatura asignatura) { this.asignatura = asignatura; }

    public void guardarBD(){
        try{
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/meninf");   
            Connection conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement("INSERT INTO Cursada (nomU, codA, estado, año, favorita, odiada) VALUES (?, ?, ?, ?, ?, ?)");

            ps.setString(1, usuario.getNombre());
            ps.setInt(2, asignatura.getCod());
            ps.setString(3, estado.name());
            ps.setInt(4, año);
            ps.setBoolean(5, favorita);
            ps.setBoolean(6, odiada);

            ps.executeUpdate();
            ps.close();
            conn.close();
        } catch (Exception e) { 
            System.err.println("Error al guardar cursada: " + e.getMessage());
        }
    }
    
}