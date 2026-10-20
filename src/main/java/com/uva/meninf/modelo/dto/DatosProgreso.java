package com.uva.meninf.modelo.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class DatosProgreso {
    public String biografia;
    public String mencion;
    public boolean tfg;
    public boolean practicas;
    public Map<Integer, String> superadas = new LinkedHashMap<>();
    public Map<Integer, String> suspensas = new LinkedHashMap<>();
    public Map<Integer, String> favoritas = new LinkedHashMap<>();
    public Map<Integer, String> odiadas   = new LinkedHashMap<>();
}