package com.Vetecare.app.model;

public enum CategoriaProducto {
    MEDICAMENTO("Medicamento"),
    VACUNA("Vacuna"),
    ALIMENTO("Alimento"),
    INSUMO("Insumo médico"),
    ACCESORIO("Accesorio");

    private final String etiqueta;

    CategoriaProducto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
