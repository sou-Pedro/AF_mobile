package com.example.af_mobile.models;

public class Lugar {

    private String id;
    private String nome;
    private String categoria;
    private double latitude;
    private double longitude;
    private String observacao;
    private String objetivo;

    public Lugar() {}

    public Lugar(String objetivo, String observacao, double longitude, double latitude, String categotia, String nome, String id) {
        this.objetivo = objetivo;
        this.observacao = observacao;
        this.longitude = longitude;
        this.latitude = latitude;
        this.categoria = categotia;
        this.nome = nome;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
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
