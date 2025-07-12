package com.lamasia.applamasia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FechaAdapter extends RecyclerView.Adapter<FechaAdapter.FechaViewHolder> {

    private List<String> fechas;
    private OnFechaClickListener listener;

    public interface OnFechaClickListener {
        void onFechaClick(int position);
    }

    public FechaAdapter(List<String> fechas, OnFechaClickListener listener) {
        this.fechas = fechas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FechaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fecha, parent, false);
        return new FechaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FechaViewHolder holder, int position) {
        holder.tvFecha.setText(fechas.get(position));
        holder.itemView.setOnClickListener(v -> listener.onFechaClick(position));
    }

    @Override
    public int getItemCount() {
        return fechas.size();
    }

    static class FechaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha;

        public FechaViewHolder(View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
