package com.uva.meninf.modelo.entidades;

import com.uva.meninf.modelo.enums.MencionTipo;

public class Mencion {

    private MencionTipo tipo;

    public Mencion(MencionTipo tipo) {
        this.tipo = tipo;
    }

    public MencionTipo getTipo() {
        return tipo;
    }
}
