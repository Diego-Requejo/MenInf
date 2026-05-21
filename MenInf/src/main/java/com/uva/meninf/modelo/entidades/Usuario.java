package com.uva.meninf.modelo.entidades;

import java.util.ArrayList;

import com.uva.meninf.modelo.relaciones.Cursada;

public class Usuario {
    private String nombre;
    private String password;
    private String biografia;
    private String mencion;
    private boolean tfg;
    private boolean practicas;

    private ArrayList<Cursada> cursadas = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
    }

    //##########################################
    //########### GETTERS Y SETTERS ############
    //##########################################

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getMencion() { return mencion; }
    public void setMencion(String mencion) { this.mencion = mencion; }

    public boolean isTfg() { return tfg; }
    public void setTfg(boolean tfg) { this.tfg = tfg; }

    public boolean isPracticas() { return practicas; }
    public void setPracticas(boolean practicas) { this.practicas = practicas; }

    public ArrayList<Cursada> getCursadas() { return cursadas; }
    public void setCursadas(ArrayList<Cursada> cursadas) { this.cursadas = cursadas; }
 
    public void añadirCursada(Cursada cursada) {
        this.cursadas.add(cursada);
    }

}