package com.lamasia.applamasia;

public class Jugador {
    private String key;       // 🔑 Clave del nodo en Firebase (necesario para acceder luego)
    private String uid;
    private String nombre;
    private String edad;
    private String categoria;
    private String urlFoto;

    public Jugador() {
        // Constructor vacío necesario para Firebase
    }

    public Jugador(String uid, String nombre, String edad, String categoria, String urlFoto) {
        this.uid = uid;
        this.nombre = nombre;
        this.edad = edad;
        this.categoria = categoria;
        this.urlFoto = urlFoto;
    }

    // Getters
    public String getKey() {
        return key;
    }

    public String getUid() {
        return uid;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEdad() {
        return edad;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    // Setters
    public void setKey(String key) {
        this.key = key;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }
}
