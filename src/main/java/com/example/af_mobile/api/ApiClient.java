package com.example.af_mobile.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public interface CallbackEndereco {
        void aoSucesso(String endereco);
        void aoErro(String erro);
    }

    public interface CallbackLugaresProximos {
        void aoSucesso(List<Lugar> lugares);
        void aoErro(String erro);
    }

    public void obterEnderecoPorCoordenadas(double latitude, double longitude, CallbackEndereco callback) {
        String url = String.format(Locale.US, "%s?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1", 
                NOMINATIM_BASE, latitude, longitude);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    String endereco = response.optString("display_name");
                    if (endereco != null && !endereco.isEmpty()) {
                        callback.aoSucesso(endereco);
                    } else {
                        callback.aoErro("Endereço não encontrado");
                    }
                },
                error -> {
                    callback.aoErro("Erro ao obter endereço");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "AF_Mobile_App_OSM_Search");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    public void buscarLugaresProximos(double latitude, double longitude, String categoria, CallbackLugaresProximos callback) {
        String query = String.format(Locale.US, "[out:json];(node['amenity'='%s'](around:2000,%f,%f););out;", 
                categoria, latitude, longitude);
        String url;
        try {
            url = OVERPASS_BASE + "?data=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            callback.aoErro("Erro na consulta");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Lugar> lugares = parseOverpassResponse(response, categoria, latitude, longitude);
                        if (lugares.isEmpty()) {
                            callback.aoErro("Nenhum lugar encontrado em 2km.");
                        } else {
                            callback.aoSucesso(lugares);
                        }
                    } catch (Exception e) {
                        callback.aoErro("Erro no processamento");
                    }
                },
                error -> {
                    callback.aoErro("Falha na busca (verifique internet)");
                });

        requestQueue.add(request);
    }

    private List<Lugar> parseOverpassResponse(JSONObject response, String categoria, double latAtual, double lonAtual) {
        List<Lugar> lista = new ArrayList<>();
        JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
        JsonArray elements = jsonObject.getAsJsonArray("elements");

        if (elements == null) return lista;

        for (int i = 0; i < elements.size(); i++) {
            JsonObject element = elements.get(i).getAsJsonObject();
            JsonObject tags = element.has("tags") ? element.getAsJsonObject("tags") : null;
            
            String nome = (tags != null && tags.has("name")) ? tags.get("name").getAsString() : "Lugar sem nome";
            double lat = element.get("lat").getAsDouble();
            double lon = element.get("lon").getAsDouble();

            Lugar lugar = new Lugar();
            lugar.setNome(nome);
            lugar.setCategoria(categoria);
            lugar.setLatitude(lat);
            lugar.setLongitude(lon);
            lugar.setDistancia(String.format(Locale.getDefault(), "%.2f km", calcularDistancia(latAtual, lonAtual, lat, lon)));

            lista.add(lugar);
        }
        return lista;
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
