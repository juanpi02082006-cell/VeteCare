package com.Vetecare.app.model;

public enum TipoMovimiento {
    ENTRADA("Entrada"),
    SALIDA("Salida");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
