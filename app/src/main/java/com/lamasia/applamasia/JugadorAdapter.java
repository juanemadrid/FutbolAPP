package com.lamasia.applamasia;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.ViewHolder> {

    public interface OnJugadorClickListener {
        void onJugadorClick(String jugadorKey);
    }

    private Context context;
    private List<Jugador> listaJugadores;
    private OnJugadorClickListener listener;

    // Constructor con listener para abrir perfil completo al hacer clic
    public JugadorAdapter(Context context, List<Jugador> listaJugadores, OnJugadorClickListener listener) {
        this.context = context;
        this.listaJugadores = listaJugadores;
        this.listener = listener;
    }

    // Constructor sin listener (opcional)
    public JugadorAdapter(Context context, List<Jugador> listaJugadores) {
        this(context, listaJugadores, null);
    }

    @NonNull
    @Override
    public JugadorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_jugador, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JugadorAdapter.ViewHolder holder, int position) {
        Jugador jugador = listaJugadores.get(position);

        holder.tvNombre.setText(jugador.getNombre());
        holder.tvEdad.setText("Edad: " + jugador.getEdad());
        holder.tvCategoria.setText("Categoría: " + jugador.getCategoria());

        if (jugador.getUrlFoto() != null && !jugador.getUrlFoto().isEmpty()) {
            Glide.with(context).load(jugador.getUrlFoto()).into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_person);
        }

        // Clic en imagen → ver información completa en DetalleJugadorActivity
        holder.ivFoto.setOnClickListener(v -> {
            if (jugador.getUid() != null) {
                Intent intent = new Intent(context, DetalleJugadorActivity.class);
                intent.putExtra("jugadorKey", jugador.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaJugadores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvEdad, tvCategoria;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvEdad = itemView.findViewById(R.id.tvEdad);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
        }
    }
}
