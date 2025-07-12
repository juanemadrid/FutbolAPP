package com.lamasia.applamasia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private List<Evento> eventos;
    private OnItemClickListener listener;

    // 🔹 Interfaz para manejar clics en los ítems (opcional)
    public interface OnItemClickListener {
        void onItemClick(Evento evento);
    }

    // 🔹 Constructor con lista inicial
    public EventoAdapter(List<Evento> eventos) {
        this.eventos = eventos;
    }

    // 🔹 Permitir establecer el listener desde la actividad
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 🔹 Actualizar lista cuando se recargan eventos
    public void actualizarLista(List<Evento> nuevosEventos) {
        this.eventos = nuevosEventos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento, parent, false);
        return new EventoViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = eventos.get(position);

        holder.tvTipo.setText(evento.getTipo());
        holder.tvFecha.setText(evento.getFecha());
        holder.tvDescripcion.setText(evento.getDescripcion());
        holder.tvHora.setText(evento.getHora());

        // 🔹 Manejar clics si el listener está definido
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(evento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventos != null ? eventos.size() : 0;
    }

    // 🔹 ViewHolder que enlaza los elementos visuales
    public static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvDescripcion, tvHora;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tvTipoEvento);
            tvFecha = itemView.findViewById(R.id.tvFechaEvento);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionEvento);
            tvHora = itemView.findViewById(R.id.tvHoraEvento);
        }
    }
}
