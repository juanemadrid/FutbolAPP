package com.lamasia.applamasia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvisoAdapter extends RecyclerView.Adapter<AvisoAdapter.ViewHolder> {

    private final List<Evento> avisos;

    public AvisoAdapter(List<Evento> avisos) {
        this.avisos = avisos;
    }

    @NonNull
    @Override
    public AvisoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AvisoAdapter.ViewHolder holder, int position) {
        Evento aviso = avisos.get(position);

        holder.tvTipo.setText("Aviso 📢");
        holder.tvFecha.setText(aviso.getFecha() != null ? aviso.getFecha() : "Sin fecha");
        holder.tvDescripcion.setText(aviso.getDescripcion());
        holder.tvHora.setText(aviso.getHora() != null ? aviso.getHora() : "");
    }

    @Override
    public int getItemCount() {
        return avisos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvDescripcion, tvHora;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoEvento);
            tvFecha = itemView.findViewById(R.id.tvFechaEvento);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionEvento);
            tvHora = itemView.findViewById(R.id.tvHoraEvento);
        }
    }
}
