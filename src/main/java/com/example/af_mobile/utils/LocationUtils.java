package com.example.af_mobile.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class LocationUtils {

    private final Activity atividade;
    private final FusedLocationProviderClient clienteLocalizacao;
    private CallbackLocalizacao callback;

    public interface CallbackLocalizacao {
        void aoSucesso(Location localizacao);
        void aoErro(String mensagemErro);
    }

    public LocationUtils(Activity atividade) {
        this.atividade = atividade;
        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(atividade);
    }

    public boolean temPermissaoLocalizacao() {
        return ContextCompat.checkSelfPermission(atividade, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void solicitarPermissao(ActivityResultLauncher<String> launcher) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void obterLocalizacaoAtual(CallbackLocalizacao callback) {
        this.callback = callback;

        if (!temPermissaoLocalizacao()) {
            callback.aoErro("Permissão de localização negada.");
            return;
        }

        try {
            clienteLocalizacao.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(localizacao -> {
                        if (localizacao != null) {
                            callback.aoSucesso(localizacao);
                        } else {
                            clienteLocalizacao.getLastLocation().addOnSuccessListener(lastLoc -> {
                                if (lastLoc != null) {
                                    callback.aoSucesso(lastLoc);
                                } else {
                                    verificarGPSeOrientar();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.aoErro("Erro ao obter localização: " + e.getMessage());
                    });
        } catch (SecurityException e) {
            callback.aoErro("Erro de segurança: " + e.getMessage());
        }
    }

    private void verificarGPSeOrientar() {
        boolean gpsHabilitado = isGPSHabilitado();
        if (!gpsHabilitado) {
            Toast.makeText(atividade, "GPS desligado. Ative para obter localização.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            atividade.startActivity(intent);
            callback.aoErro("GPS desligado.");
        } else {
            callback.aoErro("Não foi possível obter a localização. Verifique o sinal do GPS.");
        }
    }

    private boolean isGPSHabilitado() {
        android.location.LocationManager gerenciadorLocalizacao = (android.location.LocationManager)
                atividade.getSystemService(android.content.Context.LOCATION_SERVICE);
        return gerenciadorLocalizacao != null && gerenciadorLocalizacao.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
}
