package com.lamasia.applamasia;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PagosAdapter extends RecyclerView.Adapter<PagosAdapter.ViewHolder> {

    private final List<Pago> listaPagos;
    private final boolean esAdmin;
    private final Context context;

    public PagosAdapter(List<Pago> listaPagos, boolean esAdmin, Context context) {
        this.listaPagos = listaPagos;
        this.esAdmin = esAdmin;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMes, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMes = itemView.findViewById(R.id.tvMes);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }

    @NonNull
    @Override
    public PagosAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pago, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PagosAdapter.ViewHolder holder, int position) {
        Pago pago = listaPagos.get(position);
        holder.tvMes.setText(pago.getMes());

        String estado = pago.getEstado();
        String monto = estado.equals("Atrasado") ? "$70.000" : "$60.000";

        holder.tvEstado.setText(estado + " - " + monto);

        // Cambiar color según estado
        switch (estado) {
            case "Pagado":
                holder.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "Atrasado":
                holder.tvEstado.setBackgroundColor(Color.parseColor("#F44336")); // Rojo
                break;
            case "Pendiente":
                holder.tvEstado.setBackgroundColor(Color.parseColor("#FFC107")); // Amarillo
                break;
            default:
                holder.tvEstado.setBackgroundColor(Color.parseColor("#BDBDBD")); // Gris
                break;
        }

        // 🚫 CORREGIDO: Administrador no puede acceder a Wompi
        if (!esAdmin && estado.equals("Pendiente")) {
            holder.tvEstado.setOnClickListener(v -> {
                String enlaceWompi = "https://checkout.wompi.co/l/tu_enlace_aqui";
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(enlaceWompi)));
            });
        } else {
            holder.tvEstado.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaPagos.size();
    }
}
