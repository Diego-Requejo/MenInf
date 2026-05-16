package com.uva.meninf.modelo.entidades;

import com.uva.meninf.modelo.relaciones.AsignaturaMencion;

import java.util.ArrayList;

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

}
