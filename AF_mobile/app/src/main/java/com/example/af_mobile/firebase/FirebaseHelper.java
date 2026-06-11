package com.example.af_mobile.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.af_mobile.models.Lugar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class FirebaseHelper {

        private static final String TAG = "FirebaseHelper";
        private static final String COLECAO_LUGARES = "lugares_salvos";

        private final FirebaseFirestore db;
        private final CollectionReference lugaresRef;

        public FirebaseHelper() {
            db = FirebaseFirestore.getInstance();
            lugaresRef = db.collection(COLECAO_LUGARES);
        }

        // Interface para callback de operações
        public interface CallbackLugar {
            void aoSucesso();
            void aoFalha(String erro);
        }

        public interface CallbackListaLugares {
            void aoSucesso(List<Lugar> lugares);
            void aoFalha(String erro);
        }

        /**
         * Salva um novo lugar no Firestore.
         * Se o lugar já tem ID, atualiza; senão, cria um novo documento.
         */
        public void salvarLugar(Lugar lugar, CallbackLugar callback) {
            if (lugar.getId() == null || lugar.getId().isEmpty()) {
                // Criar novo documento com ID automático
                lugaresRef.add(lugar)
                        .addOnSuccessListener(documentReference -> {
                            lugar.setId(documentReference.getId());
                            callback.aoSucesso();
                            Log.d(TAG, "Lugar salvo com ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            callback.aoFalha("Erro ao salvar: " + e.getMessage());
                            Log.e(TAG, "Erro ao salvar", e);
                        });
            } else {
                // Atualizar documento existente
                lugaresRef.document(lugar.getId()).set(lugar)
                        .addOnSuccessListener(aVoid -> {
                            callback.aoSucesso();
                            Log.d(TAG, "Lugar atualizado: " + lugar.getId());
                        })
                        .addOnFailureListener(e -> {
                            callback.aoFalha("Erro ao atualizar: " + e.getMessage());
                            Log.e(TAG, "Erro ao atualizar", e);
                        });
            }
        }

        /**
         * Carrega todos os lugares salvos do Firestore.
         */
        public void carregarLugares(CallbackListaLugares callback) {
            lugaresRef.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Lugar> lista = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Lugar lugar = document.toObject(Lugar.class);
                                lugar.setId(document.getId()); // garantir que o ID do documento está setado
                                lista.add(lugar);
                            }
                            callback.aoSucesso(lista);
                            Log.d(TAG, "Lugares carregados: " + lista.size());
                        } else {
                            callback.aoFalha("Erro ao carregar: " + task.getException().getMessage());
                            Log.e(TAG, "Erro ao carregar", task.getException());
                        }
                    });
        }

        /**
         * Exclui um lugar pelo ID.
         */
        public void excluirLugar(String id, CallbackLugar callback) {
            if (id == null || id.isEmpty()) {
                callback.aoFalha("ID do lugar inválido");
                return;
            }
            lugaresRef.document(id).delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.aoSucesso();
                        Log.d(TAG, "Lugar excluído: " + id);
                    })
                    .addOnFailureListener(e -> {
                        callback.aoFalha("Erro ao excluir: " + e.getMessage());
                        Log.e(TAG, "Erro ao excluir", e);
                    });
        }

        /**
         * Busca um lugar específico pelo ID (opcional, se precisar).
         */
        public void buscarLugarPorId(String id, CallbackLugarUnico callback) {
            lugaresRef.document(id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Lugar lugar = documentSnapshot.toObject(Lugar.class);
                            lugar.setId(documentSnapshot.getId());
                            callback.aoSucesso(lugar);
                        } else {
                            callback.aoFalha("Lugar não encontrado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.aoFalha("Erro ao buscar: " + e.getMessage());
                    });
        }

        // Interface específica para buscar um único lugar
        public interface CallbackLugarUnico {
            void aoSucesso(Lugar lugar);
            void aoFalha(String erro);
        }
    }

