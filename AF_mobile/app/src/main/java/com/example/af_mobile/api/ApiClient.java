package com.example.af_mobile.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.af_mobile.models.Lugar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static final String NOMINATIM_BASE = "https://nominatim.openstreetmap.org/reverse";
    private static final String OVERPASS_BASE = "https://overpass-api.de/api/interpreter";

    private final RequestQueue requestQueue;
    private final Gson gson;

    public ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        gson = new Gson();
    }

    // Interface para retorno do endereço
    public interface CallbackEndereco {
        void aoSucesso(String endereco);
        void aoErro(String erro);
    }

    // Interface para retorno da lista de lugares próximos
    public interface CallbackLugaresProximos {
        void aoSucesso(List<Lugar> lugares);
        void aoErro(String erro);
    }

    /**
     * Obtém o endereço aproximado a partir das coordenadas (geocodificação reversa)
     * @param latitude latitude do dispositivo
     * @param longitude longitude do dispositivo
     * @param callback retorna o endereço formatado ou erro
     */
    public void obterEnderecoPorCoordenadas(double latitude, double longitude, CallbackEndereco callback) {
        String url = NOMINATIM_BASE + "?format=json&lat=" + latitude + "&lon=" + longitude + "&zoom=18&addressdetails=1";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String endereco = response.optString("display_name");
                        if (endereco != null && !endereco.isEmpty()) {
                            callback.aoSucesso(endereco);
                        } else {
                            callback.aoErro("Endereço não encontrado");
                        }
                    } catch (Exception e) {
                        callback.aoErro("Erro ao processar endereço: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Erro Nominatim: " + error.toString());
                    callback.aoErro("Falha ao obter endereço: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    /**
     * Busca lugares próximos com base na categoria (amenity) e coordenadas.
     * Raio fixo de 1000 metros.
     * @param latitude latitude atual
     * @param longitude longitude atual
     * @param categoria Categoria OSM (ex: "pharmacy", "hospital", "school", "restaurant")
     * @param callback retorna lista de lugares (modelo Lugar)
     */
    public void buscarLugaresProximos(double latitude, double longitude, String categoria, CallbackLugaresProximos callback) {
        // Overpass QL query: busca nós com a amenity especificada num raio de 1000m
        String query = "[out:json];(node['amenity'='" + categoria + "'](around:1000," + latitude + "," + longitude + "););out;";
        String url;
        try {
            url = OVERPASS_BASE + "?data=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            callback.aoErro("Erro na codificação da consulta");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Lugar> lugares = parseOverpassResponse(response, categoria, latitude, longitude);
                        if (lugares.isEmpty()) {
                            callback.aoErro("Nenhum lugar encontrado nas proximidades.");
                        } else {
                            callback.aoSucesso(lugares);
                        }
                    } catch (Exception e) {
                        callback.aoErro("Erro ao processar resposta da Overpass: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Erro Overpass: " + error.toString());
                    callback.aoErro("Falha na busca de lugares: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    /**
     * Converte o JSON retornado pela Overpass em uma lista de objetos Lugar.
     * Calcula distância aproximada entre as coordenadas.
     */
    private List<Lugar> parseOverpassResponse(JSONObject response, String categoria, double latAtual, double lonAtual) {
        List<Lugar> lista = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray elements = jsonObject.getAsJsonArray("elements");

        if (elements == null || elements.size() == 0) {
            return lista;
        }

        for (int i = 0; i < elements.size(); i++) {
            JsonObject element = elements.get(i).getAsJsonObject();
            JsonObject tags = element.getAsJsonObject("tags");
            if (tags == null) continue;

            String nome = tags.has("name") ? tags.get("name").getAsString() : "Sem nome";
            double lat = element.get("lat").getAsDouble();
            double lon = element.get("lon").getAsDouble();

            // Cria objeto Lugar (id ainda não definido, será preenchido ao salvar no Firebase)
            Lugar lugar = new Lugar();
            lugar.setNome(nome);
            lugar.setCategoria(categoria);
            lugar.setLatitude(lat);
            lugar.setLongitude(lon);
            lugar.setObservacao("");          // será preenchido pelo usuário ao salvar
            lugar.setFinalidade("");          // será preenchido pelo usuário ao salvar
            lugar.setDistancia(calcularDistancia(latAtual, lonAtual, lat, lon));

            lista.add(lugar);
        }
        return lista;
    }

    /**
     * Cálculo simples da distância euclidiana (em km) entre duas coordenadas.
     * Para uma aproximação simples, não considera curvatura da Terra.
     * Se quiser mais preciso, pode usar a fórmula de Haversine.
     */
    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio médio da Terra em km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}