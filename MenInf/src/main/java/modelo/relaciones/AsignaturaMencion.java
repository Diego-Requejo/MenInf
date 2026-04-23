package modelo.relaciones;

import modelo.entidades.Mencion;

public class AsignaturaMencion {

    private Mencion tipo;
    private int cursoAcademico;
    private boolean esObligatoria;

    public AsignaturaMencion(){}

    public Mencion getMencion() {return tipo;}
    public void setMencion(Mencion tipo) {this.tipo = tipo;}
    public int getCursoAcademico() {return cursoAcademico;}
    public void setCursoAcademico(int cursoAcademico) {this.cursoAcademico = cursoAcademico;}
    public boolean isEsObligatoria() {return esObligatoria;}
    public void setEsObligatoria(boolean esObligatoria) {this.esObligatoria = esObligatoria;}
    

}
