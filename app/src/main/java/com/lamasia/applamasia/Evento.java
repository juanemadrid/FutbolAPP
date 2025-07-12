package com.lamasia.applamasia;

public class Evento {
    private String tipo;
    private String fecha;
    private String descripcion;
    private String hora;
    private String expiracion;
    private String categoria;

    // 🔹 Constructor vacío requerido por Firebase
    public Evento() {}

    // 🔹 Constructor completo utilizado para crear eventos desde código
    public Evento(String tipo, String fecha, String descripcion, String hora, String expiracion, String categoria) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.hora = hora;
        this.expiracion = expiracion;
        this.categoria = categoria;
    }

    // 🔹 Getters (Firebase los usa internamente y también los necesitas en adapters)
    public String getTipo() {
        return tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getHora() {
        return hora;
    }

    public String getExpiracion() {
        return expiracion;
    }

    public String getCategoria() {
        return categoria;
    }

    // (Opcional: puedes agregar setters si necesitas modificar datos después de construir)
}
