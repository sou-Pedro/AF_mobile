package com.example.af_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.af_mobile.R;
import com.example.af_mobile.models.Lugar;

import java.util.List;

public class LugarAdapter extends RecyclerView.Adapter<LugarAdapter.ViewHolder> {

    private List<Lugar> lugares;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Lugar lugar);
        void onLongClick(Lugar lugar);
    }

    public LugarAdapter(List<Lugar> lugares, OnItemClickListener listener) {
        this.lugares = lugares;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lugar lugar = lugares.get(position);
        holder.tvNome.setText(lugar.getNome());
        holder.tvCategoria.setText(lugar.getCategoria());
        holder.tvCoordenadas.setText(String.format("%.4f, %.4f", lugar.getLatitude(), lugar.getLongitude()));
        holder.tvObservacao.setText(lugar.getObservacao() != null ? lugar.getObservacao() : "");

        holder.itemView.setOnClickListener(v -> listener.onClick(lugar));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(lugar);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCategoria, tvCoordenadas, tvObservacao;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvCoordenadas = itemView.findViewById(R.id.tvCoordenadas);
            tvObservacao = itemView.findViewById(R.id.tvObservacao);
        }
    }
}