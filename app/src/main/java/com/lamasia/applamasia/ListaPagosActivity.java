package com.lamasia.applamasia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ListaPagosActivity extends AppCompatActivity {

    private RecyclerView recyclerJugadores;
    private JugadorAdapter adapter;
    private ArrayList<Jugador> listaJugadores = new ArrayList<>();
    private String categoriaAdmin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_jugadores); // Puedes crear otro XML si deseas

        recyclerJugadores = findViewById(R.id.recyclerJugadores);
        recyclerJugadores.setLayoutManager(new LinearLayoutManager(this));

        adapter = new JugadorAdapter(this, listaJugadores);
        recyclerJugadores.setAdapter(adapter);

        obtenerCategoriaDelAdmin();
    }

    private void obtenerCategoriaDelAdmin() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoriaAdmin = snapshot.child("categoria").getValue(String.class);
                if (categoriaAdmin != null) {
                    cargarJugadoresDeCategoria();
                } else {
                    Toast.makeText(ListaPagosActivity.this, "Categoría no asignada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void cargarJugadoresDeCategoria() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("jugadores");
        ref.orderByChild("categoria").equalTo(categoriaAdmin)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaJugadores.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Jugador jugador = snap.getValue(Jugador.class);
                            if (jugador != null) {
                                jugador.setUid(snap.getKey()); // ⬅ UID para enviar a PagosActivity
                                listaJugadores.add(jugador);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // 🔹 Adaptador interno
    private static class JugadorAdapter extends RecyclerView.Adapter<JugadorAdapter.ViewHolder> {

        private final Context context;
        private final ArrayList<Jugador> jugadores;

        public JugadorAdapter(Context context, ArrayList<Jugador> jugadores) {
            this.context = context;
            this.jugadores = jugadores;
        }

        @NonNull
        @Override
        public JugadorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_jugador_pago, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JugadorAdapter.ViewHolder holder, int position) {
            Jugador jugador = jugadores.get(position);
            holder.txtNombre.setText(jugador.getNombre());
            holder.txtCategoria.setText(jugador.getCategoria());

            holder.btnVerPagos.setOnClickListener(v -> {
                Intent intent = new Intent(context, PagosActivity.class);
                intent.putExtra("uidJugador", jugador.getUid());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return jugadores.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtCategoria;
            Button btnVerPagos;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtNombre = itemView.findViewById(R.id.txtNombreJugador);
                txtCategoria = itemView.findViewById(R.id.txtCategoriaJugador);
                btnVerPagos = itemView.findViewById(R.id.btnVerPagos);
            }
        }
    }
}
