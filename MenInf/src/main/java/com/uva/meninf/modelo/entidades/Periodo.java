package com.uva.meninf.modelo.entidades;

import java.io.Serializable;

public class Periodo implements Serializable {

    private String idPeriodo;
    private String grupo;
    private int dia;
    private int horaIni;
    private int horaFin;
    private int codA;

    public Periodo() {}

    public String getIdPeriodo() { return idPeriodo; }
    public void setIdPeriodo(String idPeriodo) { this.idPeriodo = idPeriodo; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }     

    public int getHoraIni() { return horaIni; }
    public void setHoraIni(int horaIni) { this.horaIni = horaIni; }

    public int getHoraFin() { return horaFin; }
    public void setHoraFin(int horaFin) { this.horaFin = horaFin; }

    public int getCodA() { return codA; }
    public void setCodA(int codA) { this.codA = codA; }
}