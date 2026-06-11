package com.example.af_mobile.models;

public class Lugar {

    private String id;
    private String nome;
    private String categoria;
    private double latitude;
    private double longitude;
    private String finalidade;
    private String distancia;

    public Lugar() {}

    public Lugar(String distancia, String finalidade, double longitude, double latitude, String categoria, String nome, String id) {
        this.distancia = distancia;
        this.finalidade = finalidade;
        this.longitude = longitude;
        this.latitude = latitude;
        this.categoria = categoria;
        this.nome = nome;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistancia() {
        return distancia;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
