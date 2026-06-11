package com.example.af_mobile.activities;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.af_mobile.R;
import com.example.af_mobile.adapter.LugarAdapter;
import com.example.af_mobile.api.ApiClient;
import com.example.af_mobile.firebase.FirebaseHelper;
import com.example.af_mobile.models.Lugar;
import com.example.af_mobile.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationUtils locationUtils;
    private ApiClient apiClient;
    private FirebaseHelper firebaseHelper;

    private TextView tvLocalizacao;
    private Spinner spinnerCategoria;
    private Button btnBuscar, btnMenuBusca, btnMenuSalvos;
    private ProgressBar progressBar;
    private LinearLayout layoutBusca, layoutSalvos;

    private RecyclerView rvResultados, rvSalvos;
    private LugarAdapter adapterResultados, adapterSalvos;
    private List<Lugar> listaResultados = new ArrayList<>();
    private List<Lugar> listaSalvos = new ArrayList<>();

    private double latitudeAtual = 0, longitudeAtual = 0;
    private ActivityResultLauncher<String> launcherPermissaoLocalizacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationUtils = new LocationUtils(this);
        apiClient = new ApiClient(this);
        firebaseHelper = new FirebaseHelper();

        tvLocalizacao = findViewById(R.id.tvLocalizacao);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnMenuBusca = findViewById(R.id.btnMenuBusca);
        btnMenuSalvos = findViewById(R.id.btnMenuSalvos);
        progressBar = findViewById(R.id.progressBar);
        layoutBusca = findViewById(R.id.layoutBusca);
        layoutSalvos = findViewById(R.id.layoutSalvos);
        rvResultados = findViewById(R.id.rvResultados);
        rvSalvos = findViewById(R.id.rvSalvos);

        configurarListas();
        configurarSpinner();

        btnMenuBusca.setOnClickListener(v -> {
            layoutBusca.setVisibility(View.VISIBLE);
            layoutSalvos.setVisibility(View.GONE);
        });

        btnMenuSalvos.setOnClickListener(v -> {
            layoutBusca.setVisibility(View.GONE);
            layoutSalvos.setVisibility(View.VISIBLE);
            carregarLugaresSalvos();
        });

        btnBuscar.setOnClickListener(v -> {
            if (latitudeAtual == 0) {
                Toast.makeText(this, "Obtendo localização... Tente novamente em instantes.", Toast.LENGTH_SHORT).show();
                obterLocalizacao();
            } else {
                buscarLugares();
            }
        });

        configurarLocalizacao();
    }

    private void configurarListas() {
        adapterResultados = new LugarAdapter(listaResultados, new LugarAdapter.OnItemClickListener() {
            @Override
            public void onClick(Lugar lugar) {
                mostrarDialogSalvar(lugar);
            }
            @Override
            public void onLongClick(Lugar lugar) {}
        });
        rvResultados.setLayoutManager(new LinearLayoutManager(this));
        rvResultados.setAdapter(adapterResultados);

        adapterSalvos = new LugarAdapter(listaSalvos, new LugarAdapter.OnItemClickListener() {
            @Override
            public void onClick(Lugar lugar) {
                mostrarDialogEditar(lugar);
            }
            @Override
            public void onLongClick(Lugar lugar) {
                confirmarExclusao(lugar);
            }
        });
        rvSalvos.setLayoutManager(new LinearLayoutManager(this));
        rvSalvos.setAdapter(adapterSalvos);
    }

    private void configurarSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categorias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void configurarLocalizacao() {
        launcherPermissaoLocalizacao = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) obterLocalizacao();
                    else tvLocalizacao.setText("Sem permissão de GPS");
                });

        if (locationUtils.temPermissaoLocalizacao()) {
            obterLocalizacao();
        } else {
            locationUtils.solicitarPermissao(launcherPermissaoLocalizacao);
        }
    }

    private void obterLocalizacao() {
        tvLocalizacao.setText("Obtendo localização...");
        locationUtils.obterLocalizacaoAtual(new LocationUtils.CallbackLocalizacao() {
            @Override
            public void aoSucesso(Location localizacao) {
                latitudeAtual = localizacao.getLatitude();
                longitudeAtual = localizacao.getLongitude();
                runOnUiThread(() -> {
                    tvLocalizacao.setText(String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", latitudeAtual, longitudeAtual));
                    Toast.makeText(MainActivity.this, "Localização atualizada!", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void aoErro(String erro) {
                runOnUiThread(() -> tvLocalizacao.setText("Erro: " + erro + ". Verifique o GPS do emulador."));
            }
        });
    }

    private void buscarLugares() {
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String amenity = mapearCategoria(categoria);

        progressBar.setVisibility(View.VISIBLE);
        apiClient.buscarLugaresProximos(latitudeAtual, longitudeAtual, amenity, new ApiClient.CallbackLugaresProximos() {
            @Override
            public void aoSucesso(List<Lugar> lugares) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    listaResultados.clear();
                    listaResultados.addAll(lugares);
                    adapterResultados.notifyDataSetChanged();
                });
            }
            @Override
            public void aoErro(String erro) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, erro, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void carregarLugaresSalvos() {
        firebaseHelper.carregarLugares(new FirebaseHelper.CallbackListaLugares() {
            @Override
            public void aoSucesso(List<Lugar> lugares) {
                listaSalvos.clear();
                listaSalvos.addAll(lugares);
                adapterSalvos.notifyDataSetChanged();
            }
            @Override
            public void aoFalha(String erro) {
                Toast.makeText(MainActivity.this, "Erro ao carregar do Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogSalvar(Lugar lugar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salvar este lugar?");
        final EditText input = new EditText(this);
        input.setHint("Ex: Restaurante favorito");
        builder.setView(input);
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            lugar.setFinalidade(input.getText().toString());
            firebaseHelper.salvarLugar(lugar, new FirebaseHelper.CallbackLugar() {
                @Override
                public void aoSucesso() {
                    Toast.makeText(MainActivity.this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void aoFalha(String erro) {}
            });
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogEditar(Lugar lugar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Observação");
        final EditText input = new EditText(this);
        input.setText(lugar.getFinalidade());
        builder.setView(input);
        builder.setPositiveButton("Atualizar", (dialog, which) -> {
            lugar.setFinalidade(input.getText().toString());
            firebaseHelper.salvarLugar(lugar, new FirebaseHelper.CallbackLugar() {
                @Override
                public void aoSucesso() {
                    carregarLugaresSalvos();
                }
                @Override
                public void aoFalha(String erro) {}
            });
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void confirmarExclusao(Lugar lugar) {
        new AlertDialog.Builder(this)
                .setTitle("Remover?")
                .setMessage("Deseja apagar este lugar?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    firebaseHelper.excluirLugar(lugar.getId(), new FirebaseHelper.CallbackLugar() {
                        @Override
                        public void aoSucesso() {
                            carregarLugaresSalvos();
                        }
                        @Override
                        public void aoFalha(String erro) {}
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private String mapearCategoria(String cat) {
        switch (cat.toLowerCase()) {
            case "farmácia": return "pharmacy";
            case "hospital": return "hospital";
            case "escola": return "school";
            case "restaurante": return "restaurant";
            case "mercado": return "supermarket";
            default: return "pharmacy";
        }
    }
}
