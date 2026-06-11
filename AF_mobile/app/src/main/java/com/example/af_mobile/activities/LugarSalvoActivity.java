package com.example.af_mobile.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.af_mobile.R;
import com.example.af_mobile.adapter.LugarAdapter;
import com.example.af_mobile.firebase.FirebaseHelper;
import com.example.af_mobile.models.Lugar;

import java.util.ArrayList;
import java.util.List;

public class LugarSalvoActivity extends AppCompatActivity {

    private RecyclerView rvLugaresSalvos;
    private LugarAdapter adapter;
    private List<Lugar> listaLugares = new ArrayList<>();
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugarsalvo); // usa o layout específico

        firebaseHelper = new FirebaseHelper();
        rvLugaresSalvos = findViewById(R.id.rvLugaresSalvos);
        rvLugaresSalvos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LugarAdapter(listaLugares, new LugarAdapter.OnItemClickListener() {
            @Override
            public void onClick(Lugar lugar) {
                // Clique curto → editar observação/finalidade
                mostrarDialogEdicao(lugar);
            }

            @Override
            public void onLongClick(Lugar lugar) {
                // Clique longo → excluir
                confirmarExclusao(lugar);
            }
        });
        rvLugaresSalvos.setAdapter(adapter);

        carregarLugares();
    }

    private void carregarLugares() {
        firebaseHelper.carregarLugares(new FirebaseHelper.CallbackListaLugares() {
            @Override
            public void aoSucesso(List<Lugar> lugares) {
                listaLugares.clear();
                listaLugares.addAll(lugares);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void aoFalha(String erro) {
                Toast.makeText(LugarSalvoActivity.this, "Erro: " + erro, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogEdicao(Lugar lugar) {
        // Abre um AlertDialog com EditText para observação e Spinner para finalidade
        // Após editar, chama firebaseHelper.salvarLugar(lugar, callback)
    }

    private void confirmarExclusao(Lugar lugar) {
        // AlertDialog perguntando se confirma exclusão
        // Se sim, firebaseHelper.excluirLugar(lugar.getId(), callback)
    }
}