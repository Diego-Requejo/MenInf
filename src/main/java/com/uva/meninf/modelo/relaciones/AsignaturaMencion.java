package com.uva.meninf.modelo.relaciones;

import com.uva.meninf.modelo.entidades.Mencion;

public class AsignaturaMencion {

    private Mencion tipo;
    private int cursoAcademico;
    private boolean obligatoriedad;

    public AsignaturaMencion(){}

    public Mencion getMencion() {return tipo;}
    public void setMencion(Mencion tipo) {this.tipo = tipo;}
    public int getCursoAcademico() {return cursoAcademico;}
    public void setCursoAcademico(int cursoAcademico) {this.cursoAcademico = cursoAcademico;}
    public boolean isObligatoriedad() { return obligatoriedad; } 
    public void setObligatoriedad(boolean obligatoriedad) { this.obligatoriedad = obligatoriedad; }

}
