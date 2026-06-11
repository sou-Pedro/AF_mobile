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
import java.util.Locale;

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
        
        String info = String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", lugar.getLatitude(), lugar.getLongitude());
        if (lugar.getFinalidade() != null && !lugar.getFinalidade().isEmpty()) {
            info += "\nObs: " + lugar.getFinalidade();
        }
        holder.tvInfo.setText(info);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(lugar);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onLongClick(lugar);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvCategoria, tvInfo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }
}
