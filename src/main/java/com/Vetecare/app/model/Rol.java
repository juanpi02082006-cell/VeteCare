package com.Vetecare.app.model;

public enum Rol {
    ADMIN("Administrador"),
    VETERINARIO("Veterinario"),
    RECEPCIONISTA("Recepcionista"),
    AUXILIAR("Auxiliar de inventario"),
    PROPIETARIO("Propietario");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
