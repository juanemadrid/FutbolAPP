package com.lamasia.applamasia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ListaJugadoresActivity extends AppCompatActivity {

    private Spinner spinnerCategoria;
    private RecyclerView recyclerJugadores;
    private JugadorAdapter adapter;
    private ArrayList<Jugador> listaJugadores = new ArrayList<>();
    private String categoriaAdmin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_jugadores);

        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        recyclerJugadores = findViewById(R.id.recyclerJugadores);

        spinnerCategoria.setVisibility(View.GONE);

        recyclerJugadores.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JugadorAdapter(this, listaJugadores, jugadorKey -> {
            // Acción al hacer clic en la foto del jugador
            Intent intent = new Intent(ListaJugadoresActivity.this, GestionPerfilJugadorActivity.class);
            intent.putExtra("jugadorKey", jugadorKey);
            startActivity(intent);
        });
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
                    Toast.makeText(ListaJugadoresActivity.this, "Categoría no asignada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
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
                                jugador.setUid(snap.getKey()); // Muy importante para acceder luego
                                listaJugadores.add(jugador);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
