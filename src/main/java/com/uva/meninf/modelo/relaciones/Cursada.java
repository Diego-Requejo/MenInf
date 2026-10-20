package com.uva.meninf.modelo.relaciones;


import com.uva.meninf.modelo.entidades.Asignatura;
import com.uva.meninf.modelo.entidades.Usuario;
import com.uva.meninf.modelo.enums.EstadoAsignatura;

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

    
}