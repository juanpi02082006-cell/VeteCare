package com.Vetecare.app.model;

public enum Sexo {
    MACHO("Macho"),
    HEMBRA("Hembra");

    private final String etiqueta;

    Sexo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
