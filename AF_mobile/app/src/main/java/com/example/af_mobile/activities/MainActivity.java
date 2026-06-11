package com.example.af_mobile.activities;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.af_mobile.R;
import com.example.af_mobile.utils.LocationUtils;

public class MainActivity extends AppCompatActivity {

    private LocationUtils locationUtils;
    private TextView tvLocalizacao;
    private ActivityResultLauncher<String> launcherPermissaoLocalizacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocalizacao = findViewById(R.id.tvLocalizacao);
        locationUtils = new LocationUtils(this);

        // Registra o launcher para permissão
        launcherPermissaoLocalizacao = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        obterLocalizacao();
                    } else {
                        tvLocalizacao.setText("Permissão negada. Não é possível obter localização.");
                        Toast.makeText(this, "Permissão de localização necessária", Toast.LENGTH_SHORT).show();
                    }
                });

        // Verifica permissão
        if (!locationUtils.temPermissaoLocalizacao()) {
            locationUtils.solicitarPermissao(launcherPermissaoLocalizacao);
        } else {
            obterLocalizacao();
        }
    }

    private void obterLocalizacao() {
        locationUtils.obterLocalizacaoAtual(new LocationUtils.CallbackLocalizacao() {
            @Override
            public void aoSucesso(Location localizacao) {
                double lat = localizacao.getLatitude();
                double lon = localizacao.getLongitude();
                runOnUiThread(() -> {
                    tvLocalizacao.setText(String.format("Lat: %.6f, Lon: %.6f", lat, lon));
                    // Aqui chama a busca de lugares próximos (ApiClient)
                });
            }

            @Override
            public void aoErro(String mensagemErro) {
                runOnUiThread(() -> {
                    tvLocalizacao.setText("Erro: " + mensagemErro);
                    Toast.makeText(MainActivity.this, mensagemErro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}