package com.example.af_mobile.firebase;

import android.util.Log;
import com.example.af_mobile.models.Lugar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String COLECAO_LUGARES = "lugares_salvos";
    private final CollectionReference lugaresRef;

    public FirebaseHelper() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        lugaresRef = db.collection(COLECAO_LUGARES);
    }

    public interface CallbackLugar {
        void aoSucesso();
        void aoFalha(String erro);
    }

    public interface CallbackListaLugares {
        void aoSucesso(List<Lugar> lugares);
        void aoFalha(String erro);
    }

    public interface CallbackLugarUnico {
        void aoSucesso(Lugar lugar);
        void aoFalha(String erro);
    }

    public void salvarLugar(Lugar lugar, CallbackLugar callback) {
        if (lugar.getId() == null || lugar.getId().isEmpty()) {
            lugaresRef.add(lugar)
                    .addOnSuccessListener(documentReference -> {
                        lugar.setId(documentReference.getId());
                        callback.aoSucesso();
                    })
                    .addOnFailureListener(e -> {
                        callback.aoFalha("Erro ao salvar: " + e.getMessage());
                    });
        } else {
            lugaresRef.document(lugar.getId()).set(lugar)
                    .addOnSuccessListener(aVoid -> {
                        callback.aoSucesso();
                    })
                    .addOnFailureListener(e -> {
                        callback.aoFalha("Erro ao atualizar: " + e.getMessage());
                    });
        }
    }

    public void carregarLugares(CallbackListaLugares callback) {
        lugaresRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Lugar> lista = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Lugar lugar = document.toObject(Lugar.class);
                            if (lugar != null) {
                                lugar.setId(document.getId());
                                lista.add(lugar);
                            }
                        }
                        callback.aoSucesso(lista);
                    } else {
                        String erro = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        callback.aoFalha("Erro ao carregar: " + erro);
                    }
                });
    }

    public void excluirLugar(String id, CallbackLugar callback) {
        if (id == null || id.isEmpty()) {
            callback.aoFalha("ID do lugar inválido");
            return;
        }
        lugaresRef.document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    callback.aoSucesso();
                })
                .addOnFailureListener(e -> {
                    callback.aoFalha("Erro ao excluir: " + e.getMessage());
                });
    }

    public void buscarLugarPorId(String id, CallbackLugarUnico callback) {
        lugaresRef.document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Lugar lugar = documentSnapshot.toObject(Lugar.class);
                        if (lugar != null) {
                            lugar.setId(documentSnapshot.getId());
                            callback.aoSucesso(lugar);
                        } else {
                            callback.aoFalha("Erro ao converter os dados do lugar");
                        }
                    } else {
                        callback.aoFalha("Lugar não encontrado");
                    }
                })
                .addOnFailureListener(e -> callback.aoFalha("Erro ao buscar: " + e.getMessage()));
    }
}
